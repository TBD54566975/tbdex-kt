package tbdex.sdk.protocol.models

import com.fasterxml.jackson.databind.JsonNode
import de.fxlae.typeid.TypeId
import org.erdtman.jcs.JsonCanonicalizer
import tbdex.sdk.protocol.SignatureVerifier
import tbdex.sdk.protocol.Validator
import tbdex.sdk.protocol.models.Close.Companion.create
import tbdex.sdk.protocol.models.Rfq.Companion.create
import tbdex.sdk.protocol.serialization.Json
import web5.sdk.common.Convert
import web5.sdk.credentials.PresentationExchange
import web5.sdk.credentials.model.PresentationDefinitionV2
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.OffsetDateTime

/**
 * A class representing the Rfq message.
 * `Rfq` implements the [Message] class and contains close specific data
 * - Create message ([create])
 *
 *  @property metadata An object containing fields about the message
 *  @property data The actual message content. This will always be a JSON object.
 *                 The Message Kinds section specifies the content for each individual message type
 *  @property signature A message or resource signature is a detached compact JWS as defined in RFC-7515
 *
 * ### Example Usage:
 * ```kotlin
 * val rfq = Rfq.create(to, from, data)
 * ```
 */
@Suppress("TooGenericExceptionCaught", "SwallowedException")
class Rfq private constructor(
  override val metadata: MessageMetadata,
  override val data: RfqData,
  val privateData: RfqPrivateData? = null,
  override var signature: String? = null,
) : Message() {
  override val validNext: Set<MessageKind> = setOf(MessageKind.quote, MessageKind.close)

  /**
   * Evaluates this Rfq against the provided [Offering].
   *
   * @param offering The offering to evaluate this Rfq against.
   * @throws Exception if the Rfq doesn't satisfy the Offering's requirements
   * @throws Exception if Rfq.privateData is necessary to satisfy the Offering's requirements
   *                   and the respective privateData property is not present
   */
  fun verifyOfferingRequirements(offering: Offering) {
    require(data.offeringId == offering.metadata.id)

    if (offering.data.payin.min != null)
      check(offering.data.payin.min.toDouble() <= data.payin.amount.toDouble())

    if (offering.data.payin.max != null)
      check(offering.data.payin.max.toDouble() >= data.payin.amount.toDouble())

    validatePaymentMethod(
      data.payin.kind,
      data.payin.paymentDetailsHash,
      privateData?.payin?.paymentDetails,
      offering.data.payin.methods
    )
    validatePaymentMethod(
      data.payout.kind,
      data.payout.paymentDetailsHash,
      privateData?.payout?.paymentDetails,
      offering.data.payout.methods
    )

    offering.data.requiredClaims?.let { verifyClaims(it) }
  }

  private fun validatePaymentMethod(
    selectedMethodKind: String,
    selectedMethodDetailsHash: String?,
    selectedMethodDetails: Map<String, Any>?,
    offeredMethods: List<PaymentMethod>
  ) {
    val matchedOfferingMethod = offeredMethods.first { it.kind == selectedMethodKind }
    matchedOfferingMethod.requiredPaymentDetails?.let {
      val schema = matchedOfferingMethod.getRequiredPaymentDetailsSchema()

      if (schema == null && selectedMethodDetailsHash == null) {
        // If requiredPaymentDetails is omitted, and paymentDetails is also omitted, we have a match
        return
      }

      val jsonNodePaymentDetails = Json.jsonMapper.valueToTree<JsonNode>(selectedMethodDetails)
      schema?.validate(jsonNodePaymentDetails)
    }
  }

  private fun verifyClaims(requiredClaims: PresentationDefinitionV2) {
    // TODO check that VCs satisfying PD are crypto verified

    try {
      PresentationExchange.satisfiesPresentationDefinition(
        privateData?.claims ?: emptyList(),
        requiredClaims
      )
    } catch (e: Exception) {
      throw IllegalArgumentException("No matching claim for Offering requirements: ${requiredClaims.id}")
    }
  }

  /**
   * Verify the presence and integrity of all possible properties in Rfq.privateData
   * @throws Exception if there are properties missing in Rfq.privateData or which do not match the corresponding
   *                   hashed property in Rfq.data
   */
  fun verifyAllPrivateData() {
    // Verify payin details
    data.payin.paymentDetailsHash?.let {
      verifyPayinDetailsHash()
    }

    // Verify payout details
    data.payout.paymentDetailsHash?.let {
      verifyPayoutDetailsHash()
    }

    // Verify claims
    data.claimsHash?.let {
      verifyClaimsHash()
    }
  }

  /**
   * Verify the integrity properties that are present in
   * @throws Exception if there are properties present in Rfq.privateData which do not match the corresponding
   *                   hashed property in Rfq.data
   */
  fun verifyPresentPrivateData() {
    privateData ?: throw Error("privateData property is missing")

    // Verify payin details
    if (data.payin.paymentDetailsHash != null && privateData.payin?.paymentDetails != null) {
      verifyPayinDetailsHash()
    }

    // Verify payout details
    if (data.payout.paymentDetailsHash != null && privateData.payout?.paymentDetails != null) {
      verifyPayoutDetailsHash()
    }

    // Verify claims
    if (!data.claimsHash.isNullOrEmpty() && !privateData.claims.isNullOrEmpty()) {
      verifyClaimsHash()
    }
  }

  private fun verifyPayinDetailsHash() {
    privateData ?: throw IllegalArgumentException("privateData property is missing")
    val digest = privateData.payin?.paymentDetails?.let { digestPrivateData(privateData.salt, it) }

    if (digest != data.payin.paymentDetailsHash) {
      throw IllegalArgumentException(
        "Private data integrity check failed: " +
          "data.payin.paymentDetailsHash does not match digest of privateData.payin.paymentDetails"
      )
    }
  }

  private fun verifyPayoutDetailsHash() {
    privateData ?: throw IllegalArgumentException("privateData property is missing")
    val digest = privateData.payout?.paymentDetails?.let { digestPrivateData(privateData.salt, it) }

    if (digest != data.payout.paymentDetailsHash) {
      throw IllegalArgumentException(
        "Private data integrity check failed: " +
          "data.payout.paymentDetailsHash does not match digest of privateData.payout.paymentDetails"
      )
    }
  }

  private fun verifyClaimsHash() {
    privateData ?: throw IllegalArgumentException("privateData property is missing")

    val digest = digestPrivateData(
      privateData.salt,
      privateData.claims ?: throw IllegalArgumentException("privateData.claims is missing")
    )
    if (digest != data.claimsHash) {
      throw IllegalArgumentException(
        "Private data integrity check failed: " +
          "data.claimsHash does not match digest of privateData.claims"
      )
    }
  }

  companion object {
    /**
     * Creates a new `Rfq` message, autopopulating the id, creation time, and message kind.
     *
     * @param to DID that the message is being sent to.
     * @param from DID of the sender.
     * @param unhashedRfqData Specific parameters relevant to a Rfq.
     * @param protocol version of the tbdex protocol.
     * @param externalId external reference for the Rfq. Optional.
     * @return Rfq instance.
     */
    fun create(
      to: String,
      from: String,
      unhashedRfqData: UnhashedRfqData,
      protocol: String = "1.0",
      externalId: String? = null,
    ): Rfq {
      val id = TypeId.generate(MessageKind.rfq.name).toString()
      val metadata = MessageMetadata(
        kind = MessageKind.rfq,
        to = to,
        from = from,
        id = id,
        exchangeId = id,
        createdAt = OffsetDateTime.now(),
        protocol = protocol,
        externalId = externalId
      )

      val (data, privateData) = hashPrivateData(unhashedRfqData)

      Validator.validateData(data, "rfq")

      return Rfq(metadata, data, privateData)
    }

    /**
     * Takes an existing Message in the form of a json string and parses it into a Message object.
     * Validates object structure and performs an integrity check using the message signature.
     *
     * @param payload The message as a json string.
     * @param requireAllPrivateData If true, validate that all private data properties are present and run integrity
     *                              check.
     *                              Otherwise, only check integrity of private fields which are present.
     *                              If false or omitted, validate only the private data properties that are
     *                              currently present in `privateData`
     * @return The json string parsed into a Rfq.
     * @throws IllegalArgumentException if the payload is not valid json.
     * @throws IllegalArgumentException if the payload does not conform to the expected json schema.
     * @throws IllegalArgumentException if the payload signature verification fails.
     * @throws IllegalArgumentException if Rfq.privateData does not match Rfq.data
     */
    fun parse(payload: String, requireAllPrivateData: Boolean = false): Rfq {
      // TODO: Ensure that Message.parse() also validates private data
      val rfq = Message.parse(payload) as Rfq

      if (requireAllPrivateData) {
        rfq.verifyAllPrivateData()
      } else {
        rfq.verifyPresentPrivateData()
      }

      return rfq
    }

    private fun hashPrivateData(unhashedRfqData: UnhashedRfqData): Pair<RfqData, RfqPrivateData> {
      val salt = generateRandomSalt()

      val payinPaymentDetailsHash = unhashedRfqData.payin.paymentDetails?.let { digestPrivateData(salt, it) }
      val payoutPaymentDetailsHash = unhashedRfqData.payout.paymentDetails?.let { digestPrivateData(salt, it) }
      val claimsHash = if (unhashedRfqData.claims.isEmpty()) {
        null
      } else {
        digestPrivateData(salt, unhashedRfqData.claims)
      }

      val hashedRfqData = RfqData(
        offeringId = unhashedRfqData.offeringId,
        payin = SelectedPayinMethod(
          kind = unhashedRfqData.payin.kind,
          paymentDetailsHash = payinPaymentDetailsHash,
          amount = unhashedRfqData.payin.amount
        ),
        payout = SelectedPayoutMethod(
          kind = unhashedRfqData.payout.kind,
          paymentDetailsHash = payoutPaymentDetailsHash
        ),
        claimsHash = claimsHash
      )

      val privateRfqData = RfqPrivateData(
        salt = salt,
        payin = PrivatePaymentDetails(unhashedRfqData.payin.paymentDetails),
        payout = PrivatePaymentDetails(unhashedRfqData.payout.paymentDetails),
        claims = unhashedRfqData.claims
      )

      return Pair(hashedRfqData, privateRfqData)
    }

    private fun digestPrivateData(salt: String, value: Any): String {
      val payload = arrayOf(salt, value)
      val digest = SignatureVerifier.digestOf(payload)
      return Convert(digest).toBase64Url()
    }

    /**
     * Generate random salt, used for salted hashes in RfqPrivateData
     */
    fun generateRandomSalt(): String {
      val byteArraySize = 16
      val secureRandom = SecureRandom()
      val byteArray = ByteArray(byteArraySize)
      secureRandom.nextBytes(byteArray)
      return Convert(byteArray).toString()
    }
  }
}