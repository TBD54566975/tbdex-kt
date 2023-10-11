package models

import typeid.TypeID
import java.time.OffsetDateTime

class Quote private constructor(
  override val metadata: MessageMetadata,
  override val data: QuoteData,
  override var signature: String? = null
) : Message() {

  companion object {
    fun create(
      to: String,
      from: String,
      exchangeID: TypeID,
      data: QuoteData,
    ): Quote {
      val metadata = MessageMetadata(
        kind = MessageKind.quote,
        to = to,
        from = from,
        id = TypeID(MessageKind.quote.name).toString(),
        exchangeId = exchangeID.toString(),
        createdAt = OffsetDateTime.now()
      )

      return Quote(metadata, data)
    }
  }
}