package tbdex.sdk.protocol.models

import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.Parser
import tbdex.sdk.protocol.models.Close.Companion.create
import tbdex.sdk.protocol.models.Order.Companion.create
import tbdex.sdk.protocol.serialization.Json
import tbdex.sdk.protocol.validateExchangeId
import java.time.OffsetDateTime

/**
 * A class representing the Order message.
 * `Order` implements the [Message] class and contains close specific data
 * - Create message ([create])
 *
 *  @property metadata An object containing fields about the message
 *  @property data The actual message content. This will always be a JSON object.
 *                 The Message Kinds section specifies the content for each individual message type
 *  @property signature A message or resource signature is a detached compact JWS as defined in RFC-7515
 *
 * ### Example Usage:
 * ```kotlin
 * val order = Order.create(to, from, exchangeId)
 * ```
 */
class Order private constructor(
  override val metadata: MessageMetadata,
  override val data: OrderData,
  override var signature: String? = null
) : Message() {
  override val validNext: Set<MessageKind> = setOf(MessageKind.orderstatus)

  companion object {
    /**
     * Creates a new `Order` message, autopopulating the id, creation time, and message kind.
     *
     * @param to DID that the message is being sent to.
     * @param from DID of the sender.
     * @param exchangeId ID of the exchange.
     * @param protocol version of the tbdex protocol.
     * @param externalId external reference for the order. Optional.
     * @return Order instance.
     */
    fun create(
      to: String,
      from: String,
      exchangeId: String,
      protocol: String = "1.0",
      externalId: String? = null
    ): Order {
      validateExchangeId(exchangeId)

      val metadata = MessageMetadata(
        kind = MessageKind.order,
        to = to,
        from = from,
        id = TypeId.generate(MessageKind.order.name).toString(),
        exchangeId = exchangeId,
        createdAt = OffsetDateTime.now(),
        protocol = protocol,
        externalId = externalId
      )
      return Order(metadata, OrderData())
    }

    /**
     * Takes an existing Order in the form of a json string and parses it into an Order object.
     * Validates object structure and performs an integrity check using the message signature.
     *
     * @param payload The Order as a json string.
     * @return The json string parsed into an Order
     * @throws IllegalArgumentException if the payload is not valid json.
     * @throws IllegalArgumentException if the payload does not conform to the expected json schema.
     * @throws IllegalArgumentException if the payload signature verification fails.
     * @throws IllegalArgumentException if the payload is not an Order
     */
    fun parse(payload: String): Order {
      val jsonMessage = Parser.parseMessageToJsonNode(payload)
      val kind = jsonMessage.get("metadata").get("kind").asText()
      if (kind != "order") {
        throw IllegalArgumentException("Message must be an Order but message kind was $kind")
      }

      val message = Json.jsonMapper.convertValue(jsonMessage, Order::class.java)
      message.verify()

      return message
    }
  }
}