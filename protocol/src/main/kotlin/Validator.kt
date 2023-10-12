import exceptions.SchemaNotFoundException
import exceptions.ValidationFailedException
import models.MessageKind
import models.ResourceKind
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaClient
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject

/**
 * Utility for validating JSON payloads against predefined schemas.
 */
object Validator {
  private val schemaMap = mutableMapOf<String, Schema>()
  private const val RESOLUTION_SCOPE_URL = "classpath:/"

  /**
   * Initializes the validator by loading schemas for messages and resources.
   */
  init {
    schemaMap["message"] = loadSchema("message")
    schemaMap["resource"] = loadSchema("resource")
    
    for (messageKind in MessageKind.entries) {
      schemaMap[messageKind.name] = loadSchema(messageKind.name)
    }
    for (resourceKind in ResourceKind.entries) {
      schemaMap[resourceKind.name] = loadSchema(resourceKind.name)
    }
  }

  /**
   * Validates a JSON message against a predefined schema.
   *
   * @param jsonMessage The JSON message to validate.
   * @param schemaName The name of the schema to use for validation.
   * @throws Exception if validation fails, including a list of validation errors.
   */
  fun validate(jsonMessage: JSONObject, schemaName: String) {
    try {
      val schema = schemaMap[schemaName] ?: throw SchemaNotFoundException("No schema with name $schemaName exists")
      schema.validate(jsonMessage)
    } catch (e: ValidationException) {
      val errorList = collectValidationErrors(e)
      throw ValidationFailedException("JSON schema validation failed, errors: $errorList")
    }
  }

  /**
   * Loads a JSON schema from a resources file.
   *
   * @param kind The kind of schema to load.
   * @return The loaded JSON schema.
   */
  private fun loadSchema(kind: String): Schema {
    val schemaFile = JSONObject(
      object {}.javaClass.getResourceAsStream("$kind.schema.json")?.bufferedReader()?.readText()
    )

    return SchemaLoader.builder()
      .schemaClient(SchemaClient.classPathAwareClient())
      .resolutionScope(RESOLUTION_SCOPE_URL)
      .draftV7Support()
      .schemaJson(schemaFile)
      .build()
      .load()
      .build()
  }

  /**
   * Recursively collects validation errors from a ValidationException and its causing exceptions.
   *
   * @param e The root ValidationException to collect errors from.
   * @return A list of error messages.
   */
  private fun collectValidationErrors(e: ValidationException): List<ValidationError> {
    val errors = mutableListOf<ValidationError>()

    if (e.message != null && !e.message!!.contains("schema violations found")) {
      val error = ValidationError(e.message!!, e.pointerToViolation, e.schemaLocation)
      errors.add(error)
    }

    for (cause in e.causingExceptions) {
      errors.addAll(collectValidationErrors(cause))
    }

    return errors
  }
}

class ValidationError(
  private val message: String,
  private val pointerToViolation: String,
  private val schemaLocation: String
) {
  override fun toString(): String {
    return "Validation Error:\n" +
      "Message: $message\n" +
      "Pointer to Violation: $pointerToViolation\n" +
      "Schema Location: $schemaLocation"
  }
}
