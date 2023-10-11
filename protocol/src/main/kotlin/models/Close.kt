package models

import typeid.TypeID
import java.time.OffsetDateTime

class Close private constructor(
  override val metadata: MessageMetadata,
  override val data: CloseData,
  override var signature: String? = null
) : Message() {
  companion object {
    fun create(to: String, from: String, exchangeId: TypeID, closeData: CloseData): Close {
      val metadata = MessageMetadata(
        kind = MessageKind.close,
        to = to,
        from = from,
        id = TypeID(MessageKind.close.name).toString(),
        exchangeId = exchangeId.toString(),
        createdAt = OffsetDateTime.now()
      )
      return Close(metadata, closeData)
    }
  }
}