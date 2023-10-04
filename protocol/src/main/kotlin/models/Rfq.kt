package models

import typeid.TypeID
import java.time.OffsetDateTime

class Rfq private constructor(
  override val metadata: MessageMetadata,
  override val data: RfqData,
  private: Map<String, Any>? = null,
  override var signature: String? = null
) : Message() {

  fun verifyOfferingRequirements(offering: Offering) {
    if (offering.metadata.id != this.data.offeringID) {
      throw Exception("Offering ID mismatch. ${this.data.offeringID} !== ${offering.metadata.id} (offering)")
    }
  }

  companion object {
    fun create(
      to: String,
      from: String,
      data: RfqData,
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

      return Rfq(metadata, data, private)
    }
  }
}