import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

const val dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"

object Json {
  // has to be public in order for Json.parse<Type>() to work
  // the only other option would be to change the function signature to
  // something like `fun <T> parse(jsonString: String, typeRef: TypeReference<T>): T`
  // or `fun <T> parse(jsonString: String, clazz: Class<T>): T` which is bleh
  val objectMapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .findAndRegisterModules()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

  private val objectWriter: ObjectWriter = objectMapper.writer()

  fun stringify(obj: Any): String {
    return objectWriter.writeValueAsString(obj)
  }

  fun parse(jsonString: String): JsonNode {
    return objectMapper.readTree(jsonString)
  }
}
