package tbdex.sdk.protocol.models

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isNotNull
import com.nimbusds.jose.JWSObject
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import tbdex.sdk.protocol.TestData
import tbdex.sdk.protocol.ValidatorException
import tbdex.sdk.protocol.serialization.Json
import java.security.SignatureException
import kotlin.test.Test

class ResourceTest {

  @Test
  fun `sign populates offering signature`() {
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
  fun `sign populates balance signature`() {
    val balance = TestData.getBalance()
    balance.sign(TestData.PFI_DID)

    assertAll {
      assertThat(balance.signature).isNotNull()
      val jws = assertDoesNotThrow { JWSObject.parse(balance.signature) }
      assertThat(jws.header.algorithm).isNotNull()
      assertThat(jws.header.keyID).contains(TestData.PFI_DID.uri)
    }
  }

  @Test
  fun `parse throws error if json string is not valid`() {
    assertThrows<IllegalArgumentException> { Resource.parse(";;;;") }
  }

  @Test
  fun `offering must be signed`() {
    val offeringFromPfi = TestData.getOffering()
    // do not sign it

    val exception = assertThrows<ValidatorException> {
      Resource.parse(Json.stringify(offeringFromPfi))
    }
    assertThat(exception.message!!).contains(
      "invalid payload."
    )
  }

  @Test
  fun `balance must be signed`() {
    val balanceFromPfi = TestData.getBalance()
    // do not sign it

    val exception = assertThrows<ValidatorException> {
      Resource.parse(Json.stringify(balanceFromPfi))
    }
    assertThat(exception.message!!).contains(
      "invalid payload."
    )
  }

  @Test
  fun `offering must be signed by the sender`() {
    val offeringFromPfi = TestData.getOffering()
    //sign it with the wrong DID
    offeringFromPfi.sign(TestData.ALICE_DID)

    val exception = assertThrows<SignatureException> {
      Resource.parse(Json.stringify(offeringFromPfi))
    }
    assertThat(exception.message!!).contains(
      "Signature verification failed: Was not signed by the expected DID"
    )
  }

  @Test
  fun `balance must be signed by the sender`() {
    val balanceFromPfi = TestData.getBalance()
    //sign it with the wrong DID
    balanceFromPfi.sign(TestData.ALICE_DID)

    val exception = assertThrows<SignatureException> {
      Resource.parse(Json.stringify(balanceFromPfi))
    }
    assertThat(exception.message!!).contains("Signature verification failed: Was not signed by the expected DID")
  }
}