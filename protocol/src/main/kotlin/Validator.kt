import models.MessageKind
import models.ResourceKind
import net.pwall.json.schema.JSONSchema

// TODO: change name of `order-status.schema.json` to `orderstatus.schema.json` in tbdex repo
// TODO: update `@tbdex/protocol`  to pull in `orderstatus.schema.json` instead of `order-status.schema.json`. publish release
// TODO: profit

object Validator {
  // map of schema names -> schemas
//  private val resourceKindMap = mutableMapOf<ResourceKind, JSONSchema>() // can use mapOf<>()
//  private val messageKindMap = mutableMapOf<MessageKind, JSONSchema>() // can use mapOf<>()
  private val schemaMap = mutableMapOf<String, JSONSchema>()
  init {
    // need to add Message and Resource themselves
    for (messageKind in MessageKind.entries) {
      val resourceFile = object {}.javaClass.getResourceAsStream("${messageKind}.schema.json")?.bufferedReader()?.readText()
//      messageKindMap[messageKind] = JSONSchema.parse(resourceFile!!)
      schemaMap[messageKind.name] = JSONSchema.parse(resourceFile!!)
    }

    for (resourceKind in ResourceKind.entries) {
      val resourceFile = object {}.javaClass.getResourceAsStream("${resourceKind}.schema.json")?.bufferedReader()?.readText()
//      resourceKindMap[resourceKind] = JSONSchema.parse(resourceFile!!)
      schemaMap[resourceKind.name] = JSONSchema.parse(resourceFile!!)
    }
    val messageSchemaFile = object {}.javaClass.getResourceAsStream("message.schema.json")?.bufferedReader()?.readText()
    val resourceSchemaFile = object {}.javaClass.getResourceAsStream("resource.schema.json")?.bufferedReader()?.readText()

//    JSONSchema.parser.preLoad()
    schemaMap["message"] = JSONSchema.parse(messageSchemaFile!!)
//    schemaMap["resource"] = JSONSchema.parse(resourceSchemaFile!!)

  }

    fun validate(jsonMessage: String, schemaName: String) {

      val schema = schemaMap[schemaName] ?: throw Exception("Schema with name ${schemaName}.schema.json does not exist")
      val output = schema.validateBasic(jsonMessage)

      println("Errors ${output.errors}")
      output.errors?.forEach{
        println("${it.error} - ${it.instanceLocation}")
      }
      println("finished")
    }

}
