package tbdex.sdk.typeid

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.junit.jupiter.api.Test

class TypeIdTest {
  private val yamlMapper: ObjectMapper = ObjectMapper(YAMLFactory())

  data class Valid(val name: String, val prefix: String, val uuid: String)
  data class Invalid(val name: String, val typeId: String, val description: String)


  @Test
  fun `test valid`() {
    yamlMapper.readValue(
      TypeIdTest::class.java.getResourceAsStream("/valid.yml"), object : TypeReference<List<Valid>>() {})
      .stream()
  }

  @Test
  fun `test invalid`() {
    yamlMapper.readValue(
      TypeIdTest::class.java.getResourceAsStream("/invalid.yml"), object : TypeReference<List<Valid>>() {})
      .stream()

  }
}