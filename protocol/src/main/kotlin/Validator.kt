import Json.objectMapper
import com.fasterxml.jackson.databind.JsonNode
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import models.MessageKind
import models.ResourceKind
import org.everit.json.schema.Schema
import org.everit.json.schema.ValidationException
import org.everit.json.schema.loader.SchemaClient
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject


object Validator {
  private val schemaMap = mutableMapOf<String, JsonSchema>()
  private val factory: JsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
  init {
    for (messageKind in MessageKind.entries) {
      val resourceFile = object {}.javaClass.getResourceAsStream("${messageKind}.schema.json")?.bufferedReader()?.readText()
      val jsonNodeSchema = objectMapper.readTree(resourceFile)
      val schema = factory.getSchema(jsonNodeSchema)
      schemaMap[messageKind.name] = schema
    }

    for (resourceKind in ResourceKind.entries) {
      val resourceFile = object {}.javaClass.getResourceAsStream("${resourceKind}.schema.json")?.bufferedReader()?.readText()
      val jsonNodeSchema = objectMapper.readTree(resourceFile)
      val schema = factory.getSchema(jsonNodeSchema)
      schemaMap[resourceKind.name] = schema
    }

    val messageSchemaFile = object {}.javaClass.getResourceAsStream("message.schema.json")?.bufferedReader()?.readText()
    val messageJsonNodeSchema = objectMapper.readTree(messageSchemaFile)
    val messageSchema = factory.getSchema(messageJsonNodeSchema)
    schemaMap["message"] = messageSchema

    val resourceSchemaFile = object {}.javaClass.getResourceAsStream("resource.schema.json")?.bufferedReader()?.readText()
    val resourceJsonNodeSchema = objectMapper.readTree(resourceSchemaFile)
    val resourceSchema = factory.getSchema(resourceJsonNodeSchema)
    schemaMap["resource"] = resourceSchema

  }

  fun validate(jsonMessage: JsonNode, schemaName: String) {

    val schema = schemaMap[schemaName] ?: throw Exception("Schema with schemaName $schemaName not found")
    val errors = schema.validate(jsonMessage)

    if (errors.isNotEmpty()) {
      var errorMessages = mutableListOf<String>()
      errors.forEach {
        errorMessages.add(it.message)
      }
      throw Exception(errorMessages.joinToString(separator = ", "))
    }
  }
}

object VValidator {
  private val schemaMap = mutableMapOf<String, Schema>()




  init {
    for (messageKind in MessageKind.entries) {
      val messageSchemaJsonObject = JSONObject(object {}.javaClass.getResourceAsStream("${messageKind}.schema.json")?.bufferedReader()?.readText())
      val schema = SchemaLoader.builder()
        .schemaClient(SchemaClient.classPathAwareClient())
        .resolutionScope("classpath:/")
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
        .resolutionScope("classpath:/")
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
      .resolutionScope("classpath:/")
      .draftV7Support()
      .schemaJson(messageSchemaFile)
      .build()
      .load()
      .build()
    schemaMap["message"] = messageSchema

    val resourceSchemaFile = JSONObject(object {}.javaClass.getResourceAsStream("resource.schema.json")?.bufferedReader()?.readText())
    val resourceSchema = SchemaLoader.builder()
      .schemaClient(SchemaClient.classPathAwareClient())
      .resolutionScope("classpath:/")
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
