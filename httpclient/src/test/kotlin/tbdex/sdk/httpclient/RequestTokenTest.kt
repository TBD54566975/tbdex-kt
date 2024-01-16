package tbdex.sdk.httpclient

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import com.nimbusds.jwt.SignedJWT
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.methods.dht.DidDht
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RequestTokenTest {

  @Test
  fun `generateRequestToken() generates a JWT`() {
    val did = DidDht.create(InMemoryKeyManager())
    val pfiDid = "did:ion:123"

    val token = RequestToken.generate(did, pfiDid)
    assertNotNull(token)
  }

  @Test
  fun `generateRequestToken() generates JWT with all required fields`() {
    val did = DidDht.create(InMemoryKeyManager())
    val pfiDid = "did:ion:123"

    val token = RequestToken.generate(did, pfiDid)
    val claimsSet = SignedJWT.parse(token).jwtClaimsSet

    assertThat(claimsSet.claims.keys)
      .containsExactlyInAnyOrder(RequestToken.requiredClaimKeys)
  }

  @Test
  fun `generateRequestToken() generates JWT with fields containing correct values`() {
    val did = DidDht.create(InMemoryKeyManager())
    val pfiDid = "did:ion:123"

    val token = RequestToken.generate(did, pfiDid)
    val claimsSet = SignedJWT.parse(token).jwtClaimsSet

    assertTrue(claimsSet.issuer.contains(did.uri))
    assertTrue(claimsSet.audience.contains(pfiDid))
    assertEquals(60000, claimsSet.expirationTime.time - claimsSet.issueTime.time)
  }

  @Test
  fun `verifyRequestToken() validates given JWT token`() {
    val did = DidDht.create(InMemoryKeyManager())
    val pfiDid = "did:ion:123"

    val token = RequestToken.generate(did, pfiDid)

    val verificationResult = RequestToken.verify(token, pfiDid)

    assertEquals(did.uri, verificationResult)
  }
}