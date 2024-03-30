package tbdex.sdk.protocol.models

import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.Validator
import tbdex.sdk.protocol.models.Close.Companion.create
import tbdex.sdk.protocol.models.Quote.Companion.create
import tbdex.sdk.protocol.validateExchangeId
import java.time.OffsetDateTime

/**
 * A class representing the Quote message.
 * `Quote` implements the [Message] class and contains close specific data
 * - Create message ([create])
 *
 *  @property metadata An object containing fields about the message
 *  @property data The actual message content. This will always be a JSON object.
 *                 The Message Kinds section specifies the content for each individual message type
 *  @property signature A message or resource signature is a detached compact JWS as defined in RFC-7515
 *
 * ### Example Usage:
 * ```kotlin
 * val quote = Quote.create(to, from, exchangeId, data)
 * ```
 */
class Quote private constructor(
  override val metadata: MessageMetadata,
  override val data: QuoteData,
  override var signature: String? = null
) : Message() {
  override val validNext: Set<MessageKind> = setOf(MessageKind.order, MessageKind.close)

  companion object {
    /**
     * Creates a new `Quote` message, autopopulating the id, creation time, and message kind.
     *
     * @param to DID that the message is being sent to.
     * @param from DID of the sender.
     * @param exchangeId ID of the exchange.
     * @param quoteData Specific parameters relevant to a Quote.
     * @param protocol version of the tbdex protocol.
     * @param externalId external reference for the Quote. Optional.
     * @return Quote instance.
     */
    fun create(
      to: String,
      from: String,
      exchangeId: String,
      quoteData: QuoteData,
      protocol: String = "1.0",
      externalId: String? = null
    ): Quote {
      validateExchangeId(exchangeId)
      val metadata = MessageMetadata(
        kind = MessageKind.quote,
        to = to,
        from = from,
        id = TypeId.generate(MessageKind.quote.name).toString(),
        exchangeId = exchangeId,
        createdAt = OffsetDateTime.now(),
        protocol = protocol,
        externalId = externalId
      )
      Validator.validateData(quoteData, "quote")

      return Quote(metadata, quoteData)
    }
  }
}