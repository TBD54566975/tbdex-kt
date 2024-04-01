package tbdex.sdk.protocol.models

import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.Parser
import tbdex.sdk.protocol.Validator
import tbdex.sdk.protocol.models.Close.Companion.create
import tbdex.sdk.protocol.models.OrderStatus.Companion.create
import tbdex.sdk.protocol.serialization.Json
import tbdex.sdk.protocol.validateExchangeId
import java.time.OffsetDateTime

/**
 * A class representing the OrderStatus message.
 * `OrderStatus` implements the [Message] class and contains close specific data
 * - Create message ([create])
 *
 *  @property metadata An object containing fields about the message
 *  @property data The actual message content. This will always be a JSON object.
 *                 The Message Kinds section specifies the content for each individual message type
 *  @property signature A message or resource signature is a detached compact JWS as defined in RFC-7515
 *
 * ### Example Usage:
 * ```kotlin
 * val orderStatus = OrderStatus.create(to, from, exchangeId, data)
 * ```
 */
class OrderStatus private constructor(
  override val metadata: MessageMetadata,
  override val data: OrderStatusData,
  override var signature: String? = null
) : Message() {
  override val validNext: Set<MessageKind> = setOf(MessageKind.orderstatus, MessageKind.close)

  companion object {
    /**
     * Creates a new `OrderStatus` message, autopopulating the id, creation time, and message kind.
     *
     * @param to DID that the message is being sent to.
     * @param from DID of the sender.
     * @param exchangeId ID of the exchange.
     * @param orderStatusData Specific parameters relevant to an OrderStatus.
     * @param protocol version of the tbdex protocol.
     * @param externalId external reference for the order status. Optional.
     * @return OrderStatus instance.
     */
    fun create(
      to: String,
      from: String,
      exchangeId: String,
      orderStatusData: OrderStatusData,
      protocol: String = "1.0",
      externalId: String? = null
    ): OrderStatus {
      validateExchangeId(exchangeId)

      val metadata = MessageMetadata(
        kind = MessageKind.orderstatus,
        to = to,
        from = from,
        id = TypeId.generate(MessageKind.orderstatus.name).toString(),
        exchangeId = exchangeId,
        createdAt = OffsetDateTime.now(),
        protocol = protocol,
        externalId = externalId
      )
      Validator.validateData(orderStatusData, "orderstatus")

      return OrderStatus(metadata, orderStatusData)
    }

    /**
     * Takes an existing OrderStatus in the form of a json string and parses it into an OrderStatus object.
     * Validates object structure and performs an integrity check using the message signature.
     *
     * @param payload The OrderStatus as a json string.
     * @return The json string parsed into an OrderStatus
     * @throws IllegalArgumentException if the payload is not valid json.
     * @throws IllegalArgumentException if the payload does not conform to the expected json schema.
     * @throws IllegalArgumentException if the payload signature verification fails.
     * @throws IllegalArgumentException if the payload is not an OrderStatus
     */
    fun parse(payload: String): OrderStatus {
      val jsonMessage = Parser.parseMessageToJsonNode(payload)

      val kind = jsonMessage.get("metadata").get("kind").asText()
      if (kind != "orderstatus") {
        throw IllegalArgumentException("Message must be an OrderStatus but message kind was $kind")
      }

      val message = Json.jsonMapper.convertValue(jsonMessage, OrderStatus::class.java)
      message.verify()

      return message
    }
  }
}