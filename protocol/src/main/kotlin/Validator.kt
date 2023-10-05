import models.MessageKind
import models.ResourceKind
import net.pwall.json.schema.JSONSchema
import java.io.File


// TODO: change name of `order-status.schema.json` to `orderstatus.schema.json` in tbdex repo
// TODO: update `@tbdex/protocol`  to pull in `orderstatus.schema.json` instead of `order-status.schema.json`. publish release
// TODO: profit
object Walidator {
  init {
    val resourceFile = object {}.javaClass.getResourceAsStream("order.schema.json")?.bufferedReader()?.readText()
  }
}

// change to object
class Validator {
  // map of schema names -> schemas
  val resourceKindMap = mapOf<ResourceKind, JSONSchema>() // can use mapOf<>()

  val messageKindMap = mapOf<MessageKind, JSONSchema>() // can use mapOf<>()
  init {
    for (kind in MessageKind.values()) {

    }
  }

  companion object {

    fun validate(jsonMessage: String, schemaName: String) {
      val resourceFile = object {}.javaClass.getResourceAsStream("${schemaName}.schema.json")?.bufferedReader()?.readText()
      println(resourceFile)

//      val str = File("src/main/resources/${schemaName}.schema.json")
//      val schema2 = JSONSchema.parse(str)
//      val schema1 = JSONSchema.parseFile("src/main/resources/${schemaName}.schema.json")
//      val url = javaClass.getResource("${schemaName}.schema.json")
      val schema3 = JSONSchema.parse(resourceFile!!)
      val output = schema3.validateBasic(jsonMessage)

      println("Errors ${output.errors}")
      output.errors?.forEach{
        println("${it.error} - ${it.instanceLocation}")
      }
      println("finished")
    }

  }
}
