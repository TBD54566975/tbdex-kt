package tbdex.sdk.protocol

import com.fasterxml.jackson.databind.JsonNode
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import org.everit.json.schema.ValidationException
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.ResourceKind

/**
 * Thrown by [Validator.validate]. Note: inspect causing exception for specific [ValidationException]s
 */
class ValidatorException(message: String, cause: Exception? = null) : Exception(message, cause)

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
    try {
      val schema = schemaMap[schemaName] ?: throw ValidatorException("No schema with name $schemaName exists")

      val validationMessages = schema.validate(jsonMessage)
      println(validationMessages)

    } catch (e: ValidationException) {

      throw ValidatorException("Validation failed", e)
    }
  }
}
