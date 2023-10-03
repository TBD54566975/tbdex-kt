package models

import Mapper
import typeid.TypeID
import java.time.OffsetDateTime

class RfqData(val amount: Int) : MessageData

class Rfq private constructor(
  metadata: MessageMetadata,
  data: RfqData,
  signature: String? = null
): Message<RfqData>(metadata, data, signature) {
  companion object {
    fun create(to: String, from: String, amount: Int): Rfq {
      val id = TypeID(MessageKind.rfq.name)
      val metadata = MessageMetadata(
        kind = MessageKind.rfq,
        to = to,
        from = from,
        id = id,
        exchangeId = id,
        createdAt = OffsetDateTime.now()
      )

      val data = RfqData(amount)
      return Rfq(metadata, data)
    }

    fun parse(data: String): Rfq {
      // TODO verify the signature
      // TODO verify against json schemas

      // TODO not validated, do we need to read the subtypes individually? (metadata and data)
      return Mapper.reader(Rfq::class.java).readValue(data)
    }
  }
}