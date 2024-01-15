package tbdex.sdk.protocol

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SchemaValidatorsConfig
import com.networknt.schema.SpecVersion
import tbdex.sdk.protocol.models.Data
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.ResourceKind
import tbdex.sdk.protocol.serialization.Json
import tbdex.sdk.protocol.serialization.Json.jsonMapper
import java.net.URI

/**
 * Thrown by [Validator.validate].
 */
class ValidatorException(message: String, val errors: List<String> = listOf()) : Exception(message) {
  override val message: String? get() = "${super.message}. Errors: ${errors.joinToString()}"
}

/**
 * Utility for validating JSON payloads against predefined schemas.
 */
object Validator {
  private val schemaMap = mutableMapOf<String, JsonSchema>()
  private val config = SchemaValidatorsConfig()

  /**
   * Initializes the validator by loading schemas for messages and resources.
   */
  init {
    // Translate external URIs into internal resource URIs
    config.addUriTranslator { uri: URI ->
      val uriStr = uri.toString()
      val prefix = "https://tbdex.dev/"
      if (uriStr.startsWith(prefix)) {
        val resourceName = uriStr.substring(prefix.length)
        val resourceUri = object {}.javaClass.getResource("/$resourceName")?.toURI()
        return@addUriTranslator resourceUri
      }
      return@addUriTranslator uri
    }

    val factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)

    val definitionsStream = object {}.javaClass.getResourceAsStream("/definitions.json")
    factory.getSchema(definitionsStream, config)

    val schemaNames = listOf("message" to "message.schema.json", "resource" to "resource.schema.json") +
      MessageKind.entries.map { it.name to "${it.name}.schema.json" } +
      ResourceKind.entries.map { it.name to "${it.name}.schema.json" }

    for (schemaName in schemaNames) {
      val (name, fileName) = schemaName
      val schemaStream = object {}.javaClass.getResourceAsStream("/$fileName")
      schemaMap[name] = factory.getSchema(schemaStream, config)
    }
  }

  /**
   * Validates a JSON message against a predefined schema.
   *
   * @param jsonMessage The JSON message to validate.
   * @param schemaName The name of the schema to use for validation.
   * @throws Exception if validation fails, including a list of validation errors.
   */
  fun validate(jsonMessage: JsonNode, schemaName: String) {
    val schema = schemaMap[schemaName] ?: throw ValidatorException("No schema with name $schemaName exists")
    val validationMessages = schema.validate(jsonMessage)

    if (validationMessages.isNotEmpty()) {
      throw ValidatorException(message = "invalid payload", errors = validationMessages.map { it.message })
    }
  }

  /**
   * Validate a message against a predefined schema
   * @param message The message data to validate.
   * @throws Exception if validation fails, including a list of validation errors.
   */
  fun validateMessage(message: Message) {
    val messageJsonNode = jsonMapper.readTree(message.toString())

    validate(messageJsonNode, "message")
    validateData(message.data, message.metadata.kind.toString())
  }

  /**
   * Validate message data or resource data against a predefined schema
   * @param data The message or resource data to validate.
   * @param messageKind The message or resource kind of the data.
   * @throws Exception if validation fails, including a list of validation errors.
   */
  fun validateData(data: Data, messageKind: String) {
    val json = Json.stringify(data)
    val jsonNode = jsonMapper.readTree(json)

    this.validate(jsonNode, messageKind)
  }
}
