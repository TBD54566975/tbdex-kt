package models

import models.Close.Companion.create
import models.Quote.Companion.create
import typeid.TypeID
import java.time.OffsetDateTime

/**
 * A class representing the Quote message.
 * `Quote` implements the [Message] class and contains close specific data
 * - Create message ([create])
 *
 * ### Example Usage:
 * ```kotlin
 * val quote = Quote.create(metadata, data)
 * ```
 */
class Quote private constructor(
  override val metadata: MessageMetadata,
  override val data: QuoteData,
  override var signature: String? = null
) : Message() {

  companion object {
    /**
     * Creates a new `Quote` message, autopopulating the id, creation time, and message kind.
     *
     * @param to DID that the message is being sent to.
     * @param from DID of the sender.
     * @param exchangeId ID of the exchange.
     * @param quoteData Specific parameters relevant to a Quote.
     * @return Quote instance.
     */
    fun create(
      to: String,
      from: String,
      exchangeId: TypeID,
      quoteData: QuoteData,
    ): Quote {
      val metadata = MessageMetadata(
        kind = MessageKind.quote,
        to = to,
        from = from,
        id = TypeID(MessageKind.quote.name),
        exchangeId = exchangeId,
        createdAt = OffsetDateTime.now()
      )

      return Quote(metadata, quoteData)
    }
  }
}