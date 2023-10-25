package tbdex.sdk.protocol

import assertk.assertThat
import assertk.assertions.contains
import com.nimbusds.jose.JWSObject
import org.junit.jupiter.api.Test
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.CloseData
import tbdex.sdk.protocol.models.Message
import typeid.TypeID
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.DidIonManager

class HomelessTest {
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
    Message.parse(serializedClose) // will throw if parsing fails
  }
}