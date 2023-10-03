package website.tbd.tbdex.protocol.message_kinds

import typeid.TypeID
import java.time.OffsetDateTime
import website.tbd.tbdex.protocol.Mapper
import website.tbd.tbdex.protocol.Message
import website.tbd.tbdex.protocol.MessageKind
import website.tbd.tbdex.protocol.MessageData
import website.tbd.tbdex.protocol.MessageMetadata

class RfqData(
  val offeringID: TypeID,
  val payinSubunits: Int,
  val payinMethod: SelectedPaymentMethod,
  val payoutMethod: SelectedPaymentMethod,
  val claims: List<String>
) : MessageData

class SelectedPaymentMethod(
  val kind: String,
  val paymentDetails: Map<String, Any>
)

class Rfq private constructor(
  metadata: MessageMetadata,
  data: RfqData,
  private: Map<String, Any>? = null,
  signature: String? = null
): Message<RfqData>(metadata, data, signature) {
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

    fun parse(data: String): Rfq {
      // TODO verify the signature
      // TODO verify against json schemas

      // TODO not validated, do we need to read the subtypes individually? (metadata and data)
      return Mapper.reader(Rfq::class.java).readValue(data)
    }
  }
}