package protocol.models

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isNotNull
import com.fasterxml.jackson.core.JsonParseException
import com.nimbusds.jose.JWSObject
import models.Offering
import models.Resource
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import protocol.TestData
import kotlin.test.Test
import kotlin.test.assertIs

class ResourceTest {
  @Test
  fun `can parse a resource`() {
    val offering = TestData.getOffering()
    offering.sign(TestData.ALICE_DID)
    val resource = Resource.parse(offering.toString())

    assertIs<Offering>(resource)
  }

  @Test
  fun `sign populates resource signature`() {
    val offering = TestData.getOffering()
    offering.sign(TestData.PFI_DID)

    assertAll {
      assertThat(offering.signature).isNotNull()
      val jws = assertDoesNotThrow { JWSObject.parse(offering.signature) }
      assertThat(jws.header.algorithm).isNotNull()
      assertThat(jws.header.keyID).contains(TestData.PFI_DID.uri)
    }
  }

  @Test
  fun `parse throws error if json string is not valid`() {
    assertThrows<JsonParseException> { Resource.parse(";;;;") }
  }
}