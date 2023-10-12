package protocol.models

import com.fasterxml.jackson.core.JsonParseException
import models.Offering
import models.Resource
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import protocol.TestData
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class ResourceTest {

  @Test
  fun `can parse a list of resources`() {
    val offering1 = TestData.getOffering()
    val offering2 = TestData.getOffering()
    offering1.sign("fakepk", "fakekid")
    offering2.sign("fakepk", "fakekid")
    val resources = listOf(offering1.toJson(), offering2.toJson()).map { Resource.parse(it) }

    assertIs<Offering>(resources.first())
    assertIs<Offering>(resources.last())
  }

  @Test
  fun `parse throws error if json string is not valid`() {
    assertThrows<JsonParseException> { Resource.parse(";;;;") }
  }

  @Test
  fun `validate throws error if resource is unsigned`() {
    val exception = assertFailsWith<Exception> {
      Resource.validate(Json.stringify(TestData.getOffering()))
    }
    exception.message?.let { assertEquals("JSON schema validation failed, errors: [Validation Error:\n" +
      "Message: #/signature: expected type: String, found: Null\n" +
      "Pointer to Violation: #/signature\n" +
      "Schema Location: classpath:/#/properties/signature]", it) }
  }

  @Test
  fun `validate throws error if resource did is invalid`() {
    val exception = assertFailsWith<Exception> {
      Resource.validate(Json.stringify(TestData.getOfferingWithInvalidDid()))
    }
    exception.message?.let { assertContains(it, "does not match pattern ^did") }
  }

  @Test
  fun `can validate a list of resources`() {
    val offering1 = TestData.getOffering()
    val offering2 = TestData.getOffering()
    offering1.sign("fakepk", "fakekid")
    offering2.sign("fakepk", "fakekid")

    listOf(offering1, offering2).map {
      assertDoesNotThrow { Resource.validate(Json.stringify(it)) }
    }
  }
}
