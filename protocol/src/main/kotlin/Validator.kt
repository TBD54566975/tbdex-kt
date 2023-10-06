import Json.objectMapper
import com.fasterxml.jackson.databind.JsonNode
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import models.MessageKind
import models.ResourceKind


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
