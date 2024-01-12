package tbdex.sdk.httpclient

import com.nimbusds.jwt.SignedJWT
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.methods.dht.DidDht
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UtilsTest {

  @Test
  fun `generateRequestToken() generates a JWT`() {
    val did = DidDht.create(InMemoryKeyManager())
    val pfiDid = "did:ion:123"

    val token = generateRequestToken(did, pfiDid)
    assertNotNull(token)
  }

  @Test
  fun `generateRequestToken() generates JWT with all required fields`() {
    val did = DidDht.create(InMemoryKeyManager())
    val pfiDid = "did:ion:123"

    val token = generateRequestToken(did, pfiDid)
    val claimsSet = SignedJWT.parse(token).jwtClaimsSet

    val requiredKeys = listOf("aud", "iss", "exp", "jti", "iat")
    requiredKeys.forEach {
      assertTrue(claimsSet.claims.containsKey(it))
    }
  }

  @Test
  fun `generateRequestToken() generates JWT with fields containing correct values`() {
    val did = DidDht.create(InMemoryKeyManager())
    val pfiDid = "did:ion:123"

    val token = generateRequestToken(did, pfiDid)
    val claimsSet = SignedJWT.parse(token).jwtClaimsSet

    assertTrue(claimsSet.issuer.contains(did.uri))
    assertTrue(claimsSet.audience.contains(pfiDid))
    assertEquals(60000, claimsSet.expirationTime.time - claimsSet.issueTime.time)
  }

  @Test
  fun `verifyRequestToken() validates given JWT token`() {
    val did = DidDht.create(InMemoryKeyManager())
    val pfiDid = "did:ion:123"

    val token = generateRequestToken(did, pfiDid)

    val verificationResult = verifyRequestToken(token, pfiDid)

    assertEquals(did.uri, verificationResult)

  }

}