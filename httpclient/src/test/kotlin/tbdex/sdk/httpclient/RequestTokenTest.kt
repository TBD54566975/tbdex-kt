package tbdex.sdk.httpclient

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import com.nimbusds.jwt.SignedJWT
import io.mockk.every
import io.mockk.mockkObject
import org.junit.jupiter.api.assertThrows
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.DidResolutionResult
import web5.sdk.dids.DidResolvers
import web5.sdk.dids.methods.dht.DidDht
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RequestTokenTest {

  @Test
  fun `generate() generates a JWT`() {
    val did = DidDht.create(InMemoryKeyManager())
    val pfiDid = "did:ion:123"

    val token = RequestToken.generate(did, pfiDid)
    assertNotNull(token)
  }

  @Test
  fun `generate throws RequestTokenCreateException when assertion method not found`() {
    mockkObject(DidResolvers)

    val did = DidDht.create(InMemoryKeyManager())
    every { DidResolvers.resolve(did.uri) } returns DidResolutionResult(null)

    assertThrows<RequestTokenCreateException> {
      RequestToken.generate(did, "pfiDid", "assertionMethodId")
    }
  }

  @Test
  fun `generate() generates JWT with all required fields`() {
    val did = DidDht.create(InMemoryKeyManager())
    val pfiDid = "did:ion:123"

    val token = RequestToken.generate(did, pfiDid)
    val claimsSet = SignedJWT.parse(token).jwtClaimsSet

    assertThat(claimsSet.claims.keys)
      .containsExactlyInAnyOrder(*RequestToken.requiredClaimKeys.toTypedArray())
  }

  @Test
  fun `generate() generates JWT with fields containing correct values`() {
    val did = DidDht.create(InMemoryKeyManager())
    val pfiDid = "did:ion:123"

    val token = RequestToken.generate(did, pfiDid)
    val claimsSet = SignedJWT.parse(token).jwtClaimsSet

    assertTrue(claimsSet.issuer.contains(did.uri))
    assertTrue(claimsSet.audience.contains(pfiDid))
    assertEquals(60000, claimsSet.expirationTime.time - claimsSet.issueTime.time)
  }

  @Test
  fun `verify() validates given JWT token`() {
    val did = DidDht.create(InMemoryKeyManager())
    val pfiDid = "did:ion:123"

    val token = RequestToken.generate(did, pfiDid)

    val verificationResult = RequestToken.verify(token, pfiDid)

    assertEquals(did.uri, verificationResult)
  }

  @Test
  fun `verify() throws exception for invalid JWT token`() {

    assertThrows<RequestTokenVerificationException> {
      RequestToken.verify("abc", "did:ion:123")
    }

  }

}