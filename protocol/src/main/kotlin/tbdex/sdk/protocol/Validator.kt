package tbdex.sdk.protocol

import com.fasterxml.jackson.databind.JsonNode
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.ResourceKind

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

  /**
   * Initializes the validator by loading schemas for messages and resources.
   */
  init {
    val factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)

    val definitionsStream = object {}.javaClass.getResourceAsStream("/definitions.json")
    factory.getSchema(definitionsStream)

    val schemaNames = listOf("message" to "message.schema.json", "resource" to "resource.schema.json") +
      MessageKind.entries.map { it.name to "${it.name}.schema.json" } +
      ResourceKind.entries.map { it.name to "${it.name}.schema.json" }
    
    for (schemaName in schemaNames) {
      val (name, fileName) = schemaName
      val schemaStream = object {}.javaClass.getResourceAsStream("/$fileName")
      schemaMap[name] = factory.getSchema(schemaStream)
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
}
