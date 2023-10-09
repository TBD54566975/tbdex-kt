package models

import models.Close.Companion.create
import models.Rfq.Companion.create
import typeid.TypeID
import web5.credentials.VerifiablePresentation
import web5.credentials.model.PresentationDefinitionV2
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
class Rfq private constructor(
  override val metadata: MessageMetadata,
  override val data: RfqData,
  private: Map<String, Any>? = null,
  override var signature: String? = null
) : Message() {

  /**
   * Evaluates this Rfq against the provided [Offering].
   *
   * @param offering The offering to evaluate this Rfq against.
   * @throws Exception if the Rfq doesn't satisfy the Offering's requirements
   */
  fun verifyOfferingRequirements(offering: Offering) {
    require(data.offeringID == offering.metadata.id)

    if (offering.data.payinCurrency.minSubunits != null)
      check(offering.data.payinCurrency.minSubunits <= this.data.payinSubunits)

    if (offering.data.payinCurrency.maxSubunits != null)
      check(this.data.payinSubunits <= offering.data.payinCurrency.maxSubunits)

    validatePaymentMethod(data.payinMethod, offering.data.payinMethods)
    validatePaymentMethod(data.payoutMethod, offering.data.payoutMethods)

    this.verifyClaims(offering.data.requiredClaims)
  }

  private fun validatePaymentMethod(selectedMethod: SelectedPaymentMethod, offeringMethods: List<PaymentMethod>) {
    val matchedOfferingMethod = offeringMethods.first { it.kind == selectedMethod.kind }
    val res = matchedOfferingMethod.requiredPaymentDetails.validateBasic(selectedMethod.paymentDetails.toString())
    if (!res.errors.isNullOrEmpty()) {
      // format all errors and throw new exception
      val message =
        "rfq selected method: ${selectedMethod.kind} does not satisfy required details: ${res.errors.toString()}"
      throw IllegalArgumentException(message)
    }
  }

  private fun verifyClaims(requiredClaims: List<PresentationDefinitionV2>) {
    requiredClaims.forEach { pd ->
      val matchingVcs = VerifiablePresentation.selectFrom(pd, this.data.claims)
      require(matchingVcs.isNotEmpty()) {
        "Required claim unsatisfied: ${pd.id}}"
      }

      // verify each the matching VCs
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
      private: Map<String, Any>? = null
    ): Rfq {
      val id = TypeID(MessageKind.rfq.name)
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