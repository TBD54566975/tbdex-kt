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
  private const val resolutionScopeUrl = "classpath:/"

  init {
    for (messageKind in MessageKind.entries) {
      val messageSchemaJsonObject = JSONObject(object {}.javaClass.getResourceAsStream("${messageKind}.schema.json")?.bufferedReader()?.readText())
      val schema = SchemaLoader.builder()
        .schemaClient(SchemaClient.classPathAwareClient())
        .resolutionScope(resolutionScopeUrl)
        .draftV7Support()
        .schemaJson(messageSchemaJsonObject)
        .build()
        .load()
        .build()
      schemaMap[messageKind.name] = schema
    }

    for (resourceKind in ResourceKind.entries) {
      val resourceSchemaJsonObject = JSONObject(object {}.javaClass.getResourceAsStream("${resourceKind}.schema.json")?.bufferedReader()?.readText())
      val schema = SchemaLoader.builder()
        .schemaClient(SchemaClient.classPathAwareClient())
        .resolutionScope(resolutionScopeUrl)
        .draftV7Support()
        .schemaJson(resourceSchemaJsonObject)
        .build()
        .load()
        .build()
      schemaMap[resourceKind.name] = schema
    }

    val messageSchemaFile = JSONObject(object {}.javaClass.getResourceAsStream("message.schema.json")?.bufferedReader()?.readText())
    val messageSchema = SchemaLoader.builder()
      .schemaClient(SchemaClient.classPathAwareClient())
      .resolutionScope(resolutionScopeUrl)
      .draftV7Support()
      .schemaJson(messageSchemaFile)
      .build()
      .load()
      .build()
    schemaMap["message"] = messageSchema

    val resourceSchemaFile = JSONObject(object {}.javaClass.getResourceAsStream("resource.schema.json")?.bufferedReader()?.readText())
    val resourceSchema = SchemaLoader.builder()
      .schemaClient(SchemaClient.classPathAwareClient())
      .resolutionScope(resolutionScopeUrl)
      .draftV7Support()
      .schemaJson(resourceSchemaFile)
      .build()
      .load() // erroring here with Caused by: java.io.UncheckedIOException: java.io.FileNotFoundException: https://tbdex.io/definitions.json NO CLUE WHY
      .build()
    schemaMap["resource"] = resourceSchema
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

  fun getSchema(schemaName: String): Schema {
    return schemaMap[schemaName] ?: throw Exception("No schema with name $schemaName exists")
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
