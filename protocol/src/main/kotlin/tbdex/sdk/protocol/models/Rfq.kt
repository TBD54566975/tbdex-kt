package tbdex.sdk.protocol.models

import com.fasterxml.jackson.databind.JsonNode
import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.models.Close.Companion.create
import tbdex.sdk.protocol.models.Rfq.Companion.create
import tbdex.sdk.protocol.serialization.Json
import web5.sdk.credentials.PresentationDefinitionV2
import web5.sdk.credentials.PresentationExchange
import web5.sdk.credentials.VerifiableCredential
import java.time.OffsetDateTime

/**
 * A class representing the Rfq message.
 * `Rfq` implements the [Message] class and contains close specific data
 * - Create message ([create])
 *
 * ### Example Usage:
 * ```kotlin
 * val rfq = Rfq.create(metadata, data)
 * ```
 */
@Suppress("TooGenericExceptionCaught")

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

    if (offering.data.payinCurrency.minSubunits != null)
      check(offering.data.payinCurrency.minSubunits <= this.data.payinSubunits)

    if (offering.data.payinCurrency.maxSubunits != null)
      check(this.data.payinSubunits <= offering.data.payinCurrency.maxSubunits)

    validatePaymentMethod(data.payinMethod, offering.data.payinMethods)
    validatePaymentMethod(data.payoutMethod, offering.data.payoutMethods)

    offering.data.requiredClaims?.let { this.verifyClaims(it) }
  }

  private fun validatePaymentMethod(selectedMethod: SelectedPaymentMethod, offeringMethods: List<PaymentMethod>) {
    val matchedOfferingMethod = offeringMethods.first { it.kind == selectedMethod.kind }
    matchedOfferingMethod.requiredPaymentDetails?.let {
      val schema = matchedOfferingMethod.getRequiredPaymentDetailsSchema()
      val jsonNodePaymentDetails = Json.jsonMapper.valueToTree<JsonNode>(selectedMethod.paymentDetails)
      schema?.validate(jsonNodePaymentDetails)
    }
  }

  private fun verifyClaims(requiredClaims: PresentationDefinitionV2) {
    // check that all requirements are satisfied by one of the VC JWTs
    // and that the VC satisfying it is crypto verified

    val satisfyingClaims: MutableList<String> = mutableListOf()
    this.data.claims.forEach {
      try {
        PresentationExchange.satisfiesPresentationDefinition(it, requiredClaims)
        satisfyingClaims.add(it)
      } catch (e: Exception) {
        return@forEach
      }
    }

    if (satisfyingClaims.isEmpty()) {
      throw IllegalStateException("No matching claim for Offering requirement: ${requiredClaims.id}")
    }

    satisfyingClaims.forEach { VerifiableCredential.verify(it) }
  }

  companion object {
    /**
     * Creates a new `Rfq` message, autopopulating the id, creation time, and message kind.
     *
     * @param to DID that the message is being sent to.
     * @param from DID of the sender.
     * @param rfqData Specific parameters relevant to a Rfq.
     * @param private Sensitive information that will be ephemeral.
     * @return Rfq instance.
     */
    fun create(
      to: String,
      from: String,
      rfqData: RfqData,
      private: Map<String, Any>? = null
    ): Rfq {
      val id = TypeId.generate(MessageKind.rfq.name)
      val metadata = MessageMetadata(
        kind = MessageKind.rfq,
        to = to,
        from = from,
        id = id,
        exchangeId = id,
        createdAt = OffsetDateTime.now()
      )

      // TODO: hash `data.payinMethod.paymentDetails` and set `private`
      // TODO: hash `data.payoutMethod.paymentDetails` and set `private`

      return Rfq(metadata, rfqData, private)
    }
  }
}