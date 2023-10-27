package tbdex.sdk.protocol.models

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isNotNull
import com.nimbusds.jose.JWSObject
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import tbdex.sdk.protocol.TestData
import tbdex.sdk.protocol.serialization.Json
import java.security.SignatureException
import kotlin.test.Test

class ResourceTest {

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
    assertThrows<IllegalArgumentException> { Resource.parse(";;;;") }
  }


  @Test
  fun `resources must be signed by the sender`() {
    val offeringFromPfi = TestData.getOffering()
    //sign it with the wrong DID
    offeringFromPfi.sign(TestData.ALICE_DID)

    val exception = assertThrows<SignatureException> {
      Resource.parse(Json.stringify(offeringFromPfi))
    }
    assertThat(exception.message!!).contains("Signature verification failed: Was not signed by the expected DID")
  }
}