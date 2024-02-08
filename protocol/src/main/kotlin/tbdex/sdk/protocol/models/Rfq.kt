package tbdex.sdk.protocol.models

import com.fasterxml.jackson.databind.JsonNode
import de.fxlae.typeid.TypeId
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
 * ### Example Usage:
 * ```kotlin
 * val rfq = Rfq.create(metadata, data)
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

    if (offering.data.payinCurrency.minAmount != null)
      check(offering.data.payinCurrency.minAmount <= this.data.payinAmount)

    if (offering.data.payinCurrency.maxAmount != null)
      check(this.data.payinAmount <= offering.data.payinCurrency.maxAmount)

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
    // TODO check that VCs satisfying PD are crypto verified

    try {
      PresentationExchange.satisfiesPresentationDefinition(this.data.claims, requiredClaims)
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
     * @param private Sensitive information that will be ephemeral.
     * @return Rfq instance.
     */
    fun create(
      to: String,
      from: String,
      rfqData: RfqData,
      externalId: String? = null,
      private: Map<String, Any>? = null
    ): Rfq {
      val id = TypeId.generate(MessageKind.rfq.name)
      val metadata = MessageMetadata(
        kind = MessageKind.rfq,
        to = to,
        from = from,
        id = id,
        exchangeId = id,
        createdAt = OffsetDateTime.now(),
        externalId = externalId
      )
      Validator.validateData(rfqData, "rfq")

      // TODO: hash `data.payinMethod.paymentDetails` and set `private`
      // TODO: hash `data.payoutMethod.paymentDetails` and set `private`

      return Rfq(metadata, rfqData, private)
    }
  }
}