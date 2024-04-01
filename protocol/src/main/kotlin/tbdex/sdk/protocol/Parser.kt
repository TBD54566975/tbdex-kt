package tbdex.sdk.protocol

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import tbdex.sdk.protocol.models.Balance
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.OrderStatus
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.Resource
import tbdex.sdk.protocol.models.ResourceKind
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.serialization.Json

/**
 * Utility functions for parsing TBDex Messages and Resources
 */
object Parser {
  /**
   * Takes an existing Message in the form of a json string and parses it into a Message object.
   * Validates object structure and performs an integrity check using the message signature.
   *
   * @param payload The message as a json string.
   * @return The json string parsed into a concrete Message implementation.
   * @throws IllegalArgumentException if the payload is not valid json.
   * @throws IllegalArgumentException if the payload does not conform to the expected json schema.
   * @throws IllegalArgumentException if the payload signature verification fails.
   */
  fun parseMessage(payload: String): Message {
    val jsonMessage = parseMessageToJsonNode(payload)
    val kind = jsonMessage.get("metadata").get("kind").asText()

    val messageType = when (MessageKind.valueOf(kind)) {
      MessageKind.rfq -> Rfq::class.java
      MessageKind.order -> Order::class.java
      MessageKind.orderstatus -> OrderStatus::class.java
      MessageKind.quote -> Quote::class.java
      MessageKind.close -> Close::class.java
    }

    val message = Json.jsonMapper.convertValue(jsonMessage, messageType)
    message.verify()

    return message
  }

  /**
   * Takes an existing Message in the form of a json string and parses it into a JsonNode object.
   * Validates object structure.
   *
   * @param payload The message as a json string.
   * @return The json string parsed into a JsonNode object.
   * @throws IllegalArgumentException if the payload is not valid json.
   * @throws IllegalArgumentException if the payload does not conform to the expected json schema.
   */
  fun parseMessageToJsonNode(payload: String): JsonNode {
    val jsonMessage: JsonNode

    try {
      jsonMessage = Json.jsonMapper.readTree(payload)
    } catch (e: JsonParseException) {
      throw IllegalArgumentException("unexpected character at offset ${e.location.charOffset}")
    }

    require(jsonMessage.isObject) { "expected payload to be a json object" }

    // validate message structure
    Validator.validate(jsonMessage, "message")

    val jsonMessageData = jsonMessage.get("data")
    val kind = jsonMessage.get("metadata").get("kind").asText()

    // validate specific message data (Rfq, Quote, etc)
    Validator.validate(jsonMessageData, kind)

    return jsonMessage
  }

  /**
   * Takes an existing Resource in the form of a json string and parses it into a Resource object.
   * Validates object structure and performs an integrity check using the resource signature.
   *
   * @param payload The resource as a json string.
   * @return The json string parsed into a concrete Resource implementation.
   * @throws IllegalArgumentException if the payload is not valid json.
   * @throws IllegalArgumentException if the payload does not conform to the expected json schema.
   * @throws IllegalArgumentException if the payload signature verification fails.
   */
  fun parseResource(payload: String): Resource {
    val jsonResource: JsonNode = try {
      Json.jsonMapper.readTree(payload)
    } catch (e: JsonParseException) {
      throw IllegalArgumentException("unexpected character at offset ${e.location.charOffset}")
    }

    require(jsonResource.isObject) { "expected payload to be a json object" }

    // validate message structure
    Validator.validate(jsonResource, "resource")

    val dataJson = jsonResource.get("data")
    val kind = jsonResource.get("metadata").get("kind").asText()

    // validate specific resource data
    Validator.validate(dataJson, kind)

    val resourceType = when (ResourceKind.valueOf(kind)) {
      ResourceKind.offering -> Offering::class.java
      ResourceKind.balance -> Balance::class.java
    }

    val resource = Json.jsonMapper.convertValue(jsonResource, resourceType)
    resource.verify()

    return resource
  }

  /**
   * Takes an existing REsource in the form of a json string and parses it into a JsonNode object.
   * Validates object structure.
   *
   * @param payload The resource as a json string.
   * @return The json string parsed into a JsonNode object.
   * @throws IllegalArgumentException if the payload is not valid json.
   * @throws IllegalArgumentException if the payload does not conform to the expected json schema.
   */
  fun parseResourceToJsonNode(payload: String): JsonNode {
    val jsonResource: JsonNode

    try {
      jsonResource = Json.jsonMapper.readTree(payload)
    } catch (e: JsonParseException) {
      throw IllegalArgumentException("unexpected character at offset ${e.location.charOffset}")
    }

    require(jsonResource.isObject) { "expected payload to be a json object" }

    // validate resource structure
    Validator.validate(jsonResource, "resource")

    val jsonResourceData = jsonResource.get("data")
    val kind = jsonResource.get("metadata").get("kind").asText()

    // validate specific message data (Rfq, Quote, etc)
    Validator.validate(jsonResourceData, kind)

    return jsonResource
  }
}