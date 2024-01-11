package tbdex.sdk.httpclient

import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.methods.dht.DidDht
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class UtilsTest {

  @Test
  fun `generateRequestToken() generates a JWT`() {
    val did = DidDht.create(InMemoryKeyManager())
    val pfiDid = "did:ion:123"

    val token = generateRequestToken(did, pfiDid)
    println(token)
    assertNotNull(token)
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