import models.GetOfferingsOptions
import org.junit.jupiter.api.Disabled
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.DidKey
import kotlin.test.Test

class RealTbdexClientTest {
  // TODO replace with didion and set the service
  private val pfi = DidKey.create(InMemoryKeyManager())

  @Test
  @Disabled
  fun `can get offerings`() {
    RealTbdexClient.getOfferings(GetOfferingsOptions(pfi.uri))
  }

  @Test
  @Disabled
  fun `can send message`() {
    TODO()
  }

  @Test
  @Disabled
  fun `can get exchange`() {
    TODO()
  }

  @Test
  @Disabled
  fun `can get exchanges`() {
    TODO()
  }
}