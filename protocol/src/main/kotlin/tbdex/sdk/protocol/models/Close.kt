package tbdex.sdk.protocol.models

import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.Validator
import tbdex.sdk.protocol.models.Close.Companion.create
import java.time.OffsetDateTime

/**
 * A class representing the Close message.
 * `Close` implements the [Message] class and contains close specific data
 * - Create message ([create])
 *
 * ### Example Usage:
 *
 * ```kotlin
 * val close = Close.create(metadata, data)
 * ```
 */
class Close private constructor(
  override val metadata: MessageMetadata,
  override val data: CloseData,
  override var signature: String? = null
) : Message() {
  override val validNext: Set<MessageKind> = emptySet()

  companion object {
    /**
     * Creates a new `Close` message, autopopulating the id, creation time, and message kind.
     *
     * @param to DID that the message is being sent to.
     * @param from DID of the sender.
     * @param exchangeId ID of the exchange.
     * @param closeData Specific parameters relevant to a Close.
     * @return Close instance.
     */
    fun create(to: String, from: String, exchangeId: TypeId, closeData: CloseData, externalId: String? = null): Close {
      val metadata = MessageMetadata(
        kind = MessageKind.close,
        to = to,
        from = from,
        id = TypeId.generate(MessageKind.close.name),
        exchangeId = exchangeId,
        createdAt = OffsetDateTime.now(),
        externalId = externalId
      )
      Validator.validateData(closeData, "close")
      return Close(metadata, closeData)
    }
  }
}