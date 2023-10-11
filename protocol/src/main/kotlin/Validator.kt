import com.fasterxml.jackson.databind.JsonNode
import models.MessageKind
import models.ResourceKind
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaClient
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject

object Validator {
  private val schemaMap = mutableMapOf<String, Schema>()
  private const val RESOLUTION_SCOPE_URL = "classpath:/"

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

  fun validate(jsonMessage: JSONObject, schemaName: String) {
    try {
      val schema = schemaMap[schemaName] ?: throw Exception("No schema with name $schemaName exists")
      schema.validate(jsonMessage)
    } catch (e: ValidationException) {
      val errorList = collectValidationErrors(e)
      throw Exception("Validation failed, errors: $errorList")
    }
  }

  private fun loadSchema(kind: String): Schema {
    val schemaFile = JSONObject(object {}.javaClass.getResourceAsStream("$kind.schema.json")?.bufferedReader()?.readText())

    return SchemaLoader.builder()
      .schemaClient(SchemaClient.classPathAwareClient())
      .resolutionScope(RESOLUTION_SCOPE_URL)
      .draftV7Support()
      .schemaJson(schemaFile)
      .build()
      .load()
      .build()
  }

  private fun collectValidationErrors(e: ValidationException): List<String> {
    val errors = mutableListOf<String>()

    if (e.message != null && !e.message!!.contains("schema violations found")) {
      errors.add(e.message!!)
    }

    for (cause in e.causingExceptions) {
      errors.addAll(collectValidationErrors(cause))
    }

    return errors
  }
}
