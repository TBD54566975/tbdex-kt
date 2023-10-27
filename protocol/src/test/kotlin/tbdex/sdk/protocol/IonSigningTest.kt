package tbdex.sdk.protocol

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import com.nimbusds.jose.JWSObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.CloseData
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.Resource
import typeid.TypeID
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.DidIonManager
import kotlin.test.assertIs

class IonSigningTest {
  @Test
  fun `can sign a tbdex message with an ion did`() {
    val km = InMemoryKeyManager()
    val did = DidIonManager.create(km)

    val close = Close.create(
      to = "did:ex:pfi",
      from = did.uri,
      exchangeId = TypeID("offering"),
      closeData = CloseData(reason = "hi")
    )

    close.sign(did)

    val jwsObject = JWSObject.parse(close.signature)
    assertThat(jwsObject.header.keyID).contains(did.uri)

    val serializedClose = close.toString()
    assertDoesNotThrow { Message.parse(serializedClose) }
  }

  @Test
  fun `can parse ION signed offering from a json string`() {
    val did = DidIonManager.create(InMemoryKeyManager())
    val offering = TestData.getOffering(from = did.uri)
    offering.sign(did)
    val jsonResource = offering.toString()
    val parsed = Resource.parse(jsonResource)

    assertIs<Offering>(parsed)
    assertThat(parsed.toString()).isEqualTo(jsonResource)
  }
}