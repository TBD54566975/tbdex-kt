package tbdex.sdk.protocol.models

import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.Parser
import tbdex.sdk.protocol.Validator
import tbdex.sdk.protocol.models.Close.Companion.create
import tbdex.sdk.protocol.serialization.Json
import tbdex.sdk.protocol.validateExchangeId
import java.time.OffsetDateTime

/**
 * A class representing the Close message.
 * `Close` implements the [Message] class and contains close specific data
 * - Create message ([create])
 *
 *  @property metadata An object containing fields about the message
 *  @property data The actual message content. This will always be a JSON object.
 *                 The Message Kinds section specifies the content for each individual message type
 *  @property signature A message or resource signature is a detached compact JWS as defined in RFC-7515
 *
 * ### Example Usage:
 *
 * ```kotlin
 * val close = Close.create(to, from, exchangeId, data)
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
     * @param protocol version of the tbdex protocol.
     * @param externalId external reference for the Close. Optional.
     * @return Close instance.
     */
    fun create(
      to: String,
      from: String,
      exchangeId: String,
      closeData: CloseData,
      protocol: String = "1.0",
      externalId: String? = null
    ): Close {
      validateExchangeId(exchangeId)

      val metadata = MessageMetadata(
        kind = MessageKind.close,
        to = to,
        from = from,
        id = TypeId.generate(MessageKind.close.name).toString(),
        exchangeId = exchangeId,
        createdAt = OffsetDateTime.now(),
        protocol = protocol,
        externalId = externalId
      )
      Validator.validateData(closeData, "close")
      return Close(metadata, closeData)
    }

    /**
     * Takes an existing Close in the form of a json string and parses it into a Close object.
     * Validates object structure and performs an integrity check using the message signature.
     *
     * @param payload The Close as a json string.
     * @return The json string parsed into a Close
     * @throws IllegalArgumentException if the payload is not valid json.
     * @throws IllegalArgumentException if the payload does not conform to the expected json schema.
     * @throws IllegalArgumentException if the payload signature verification fails.
     * @throws IllegalArgumentException if the payload is not a Close
     */
    fun parse(payload: String): Close {
      val jsonMessage = Parser.parseMessageToJsonNode(payload)

      val kind = jsonMessage.get("metadata").get("kind").asText()
      if (kind != "close") {
        throw IllegalArgumentException("Message must be a Close but message kind was $kind")
      }

      val message = Json.jsonMapper.convertValue(jsonMessage, Close::class.java)
      message.verify()

      return message
    }
  }
}