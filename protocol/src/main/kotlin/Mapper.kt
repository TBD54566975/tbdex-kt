import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

const val dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"

object Mapper {
    // TODO make private and add method for calling readTree
    val objectMapper: ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .findAndRegisterModules()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    fun writer(): ObjectWriter {
        return objectMapper.writer()
    }

    fun <T> reader(type: Class<T>): ObjectReader {
        return objectMapper.readerFor(type)
    }


}