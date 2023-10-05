import net.pwall.json.schema.JSONSchema
import java.io.File


class Validator {

  companion object {

    fun validate(jsonMessage: String, schemaName: String) {

//      val str = File("src/main/resources/${schemaName}.schema.json")
//      val schema2 = JSONSchema.parse(str)
//      val schema1 = JSONSchema.parseFile("src/main/resources/${schemaName}.schema.json")


      val url = javaClass.getResource("/${schemaName}.schema.json")
      val schema3 = JSONSchema.parse(jsonMessage, url?.toURI())
      val output = schema3.validateBasic(jsonMessage)

      println("Errors ${output.errors}")
      output.errors?.forEach{
        println("${it.error} - ${it.instanceLocation}")
      }
      println("finished")
    }

  }
}
