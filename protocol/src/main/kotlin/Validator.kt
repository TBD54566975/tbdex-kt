import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import models.MessageKind
import models.ResourceKind

// TODO: change name of `order-status.schema.json` to `orderstatus.schema.json` in tbdex repo
// TODO: update `@tbdex/protocol`  to pull in `orderstatus.schema.json` instead of `order-status.schema.json`. publish release
// TODO: profit

object Validator {
  private val schemaMap = mutableMapOf<String, JsonSchema>()
  private val factory: JsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7)
  init {
    for (messageKind in MessageKind.entries) {
      val resourceFile = object {}.javaClass.getResourceAsStream("${messageKind}.schema.json")?.bufferedReader()?.readText()
      val jsonNodeSchema = Json.objectMapper.readTree(resourceFile)
      val schema = factory.getSchema(jsonNodeSchema)
      schemaMap[messageKind.name] = schema
    }

    for (resourceKind in ResourceKind.entries) {
      val resourceFile = object {}.javaClass.getResourceAsStream("${resourceKind}.schema.json")?.bufferedReader()?.readText()
      val jsonNodeSchema = Json.objectMapper.readTree(resourceFile)
      val schema = factory.getSchema(jsonNodeSchema)
      schemaMap[resourceKind.name] = schema
    }

    val messageSchemaFile = object {}.javaClass.getResourceAsStream("message.schema.json")?.bufferedReader()?.readText()
    val messageJsonNodeSchema = Json.objectMapper.readTree(messageSchemaFile)
    val messageSchema = factory.getSchema(messageJsonNodeSchema)
    schemaMap["message"] = messageSchema

    val resourceSchemaFile = object {}.javaClass.getResourceAsStream("resource.schema.json")?.bufferedReader()?.readText()
    val resourceJsonNodeSchema = Json.objectMapper.readTree(resourceSchemaFile)
    val resourceSchema = factory.getSchema(resourceJsonNodeSchema)
    schemaMap["resource"] = resourceSchema

  }

  fun validate(jsonMessage: String, schemaName: String) {

    val schema = schemaMap[schemaName] ?: throw Exception("Schema with schemaName $schemaName not found")
    schema.initializeValidators()
    val jsonNodeMessage = Json.objectMapper.readTree(jsonMessage)
    val errors = schema.validate(jsonNodeMessage)
    errors.forEach {
      println("${it.message}, ${it.type}")
    }
  }
}
