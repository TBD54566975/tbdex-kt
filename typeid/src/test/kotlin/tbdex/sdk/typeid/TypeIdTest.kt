package tbdex.sdk.typeid

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TypeIdTest {
  private val yamlMapper: ObjectMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
  private val jsonMapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .findAndRegisterModules()

  data class Valid(val name: String, val typeid: String, val prefix: String, val uuid: String)
  data class Invalid(val name: String, val typeid: String, val description: String)

  @Nested
  inner class SpecTest {
    @Test
    fun `valid inputs`() {
      val loader = Thread.currentThread().contextClassLoader
      val vectorsJson = loader.getResourceAsStream("valid.json")?.bufferedReader()?.readText()!!
      val stream: ArrayList<Valid> = jsonMapper.readValue(
        vectorsJson,
        jsonMapper.typeFactory.constructCollectionType(List::class.java, Valid::class.java)
      )

      stream.forEach {
        println("Testing valid: ${it.name}, ${it.typeid}")
        var actual = TypeId.fromString(it.typeid)
        assertAll {
          assertThat(actual.type).isEqualTo(it.prefix)
          assertThat(actual.toString()).isEqualTo(it.typeid)
          assertThat(actual.toUUID()).isEqualTo(it.uuid)
        }

        println("Testing valid: ${it.name}, ${it.uuid}")
        actual = TypeId.fromUUID(it.prefix, it.uuid)
        assertAll {
          assertThat(actual.type).isEqualTo(it.prefix)
          assertThat(actual.toString()).isEqualTo(it.typeid)
          assertThat(actual.toUUID()).isEqualTo(it.uuid)
        }
      }
    }

    @Test
    fun `invalid inputs`() {
      val loader = Thread.currentThread().contextClassLoader
      val vectorsJson = loader.getResourceAsStream("invalid.json")?.bufferedReader()?.readText()!!
      val stream: ArrayList<Invalid> = jsonMapper.readValue(
        vectorsJson,
        jsonMapper.typeFactory.constructCollectionType(List::class.java, Invalid::class.java)
      )

      stream.forEach {
        println("Testing invalid: ${it.name}, ${it.typeid}")
        assertThrows<IllegalArgumentException> { TypeId.fromString(it.typeid) }
      }
    }
  }
}