package tbdex.sdk.protocol.models

import com.fasterxml.jackson.databind.JsonNode
import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.Parser
import tbdex.sdk.protocol.Validator
import tbdex.sdk.protocol.models.Close.Companion.create
import tbdex.sdk.protocol.models.Rfq.Companion.create
import tbdex.sdk.protocol.serialization.Json
import web5.sdk.credentials.PresentationExchange
import web5.sdk.credentials.model.PresentationDefinitionV2
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
  private: Map<String, Any>? = null,
  override var signature: String? = null
) : Message() {
  override val validNext: Set<MessageKind> = setOf(MessageKind.quote, MessageKind.close)

  /**
   * Evaluates this Rfq against the provided [Offering].
   *
   * @param offering The offering to evaluate this Rfq against.
   * @throws Exception if the Rfq doesn't satisfy the Offering's requirements
   */
  fun verifyOfferingRequirements(offering: Offering) {
    require(data.offeringId == offering.metadata.id)

    if (offering.data.payin.min != null)
      check(offering.data.payin.min.toDouble() <= data.payin.amount.toDouble())

    if (offering.data.payin.max != null)
      check(offering.data.payin.max.toDouble() >= data.payin.amount.toDouble())

    validatePaymentMethod(data.payin, offering.data.payin.methods)
    validatePaymentMethod(data.payout, offering.data.payout.methods)

    offering.data.requiredClaims?.let { verifyClaims(it) }
  }

  private fun validatePaymentMethod(selectedMethod: SelectedPaymentMethod, offeredMethods: List<PaymentMethod>) {
    val matchedOfferingMethod = offeredMethods.first { it.kind == selectedMethod.kind }
    matchedOfferingMethod.requiredPaymentDetails?.let {
      val schema = matchedOfferingMethod.getRequiredPaymentDetailsSchema()
      val jsonNodePaymentDetails = Json.jsonMapper.valueToTree<JsonNode>(selectedMethod.paymentDetails)
      schema?.validate(jsonNodePaymentDetails)
    }
  }

  private fun verifyClaims(requiredClaims: PresentationDefinitionV2) {
    // TODO check that VCs satisfying PD are crypto verified

    try {
      PresentationExchange.satisfiesPresentationDefinition(data.claims, requiredClaims)
    } catch (e: Exception) {
      throw IllegalArgumentException("No matching claim for Offering requirements: ${requiredClaims.id}")
    }
  }

  companion object {
    /**
     * Creates a new `Rfq` message, autopopulating the id, creation time, and message kind.
     *
     * @param to DID that the message is being sent to.
     * @param from DID of the sender.
     * @param rfqData Specific parameters relevant to a Rfq.
     * @param protocol version of the tbdex protocol.
     * @param externalId external reference for the Rfq. Optional.
     * @param private Sensitive information that will be ephemeral.
     * @return Rfq instance.
     */
    fun create(
      to: String,
      from: String,
      rfqData: RfqData,
      protocol: String = "1.0",
      externalId: String? = null,
      private: Map<String, Any>? = null
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
      Validator.validateData(rfqData, "rfq")

      // TODO: hash `data.payinMethod.paymentDetails` and set `private`
      // TODO: hash `data.payoutMethod.paymentDetails` and set `private`

      return Rfq(metadata, rfqData, private)
    }

    /**
     * Takes an existing Rfq in the form of a json string and parses it into a Rfq object.
     * Validates object structure and performs an integrity check using the message signature.
     *
     * @param payload The Rfq as a json string.
     * @return The json string parsed into a Rfq
     * @throws IllegalArgumentException if the payload is not valid json.
     * @throws IllegalArgumentException if the payload does not conform to the expected json schema.
     * @throws IllegalArgumentException if the payload signature verification fails.
     * @throws IllegalArgumentException if the payload is not an RFQ
     */
    fun parse(payload: String): Rfq {
      val jsonMessage = Parser.parseMessageToJsonNode(payload)

      val kind = jsonMessage.get("metadata").get("kind").asText()
      if (kind != "rfq") {
        throw IllegalArgumentException("Message must be an RFQ but message kind was $kind")
      }

      val message = Json.jsonMapper.convertValue(jsonMessage, Rfq::class.java)
      message.verify()

      return message
    }
  }
}