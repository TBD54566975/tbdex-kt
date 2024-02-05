package tbdex.sdk.protocol.models

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import de.fxlae.typeid.TypeId
import org.junit.jupiter.api.assertDoesNotThrow
import tbdex.sdk.protocol.TestData
import tbdex.sdk.protocol.serialization.Json
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertIs

class QuoteTest {
  @Test
  fun `can create a new quote`() {
    val quote = Quote.create(
      to = "pfi",
      from = "alice",
      exchangeId = TypeId.generate(MessageKind.rfq.name),
      quoteData = QuoteData(
        expiresAt = OffsetDateTime.now().plusDays(1),
        payin = QuoteDetails("AUD", "10.00", "0.0"),
        payout = QuoteDetails("BTC", "0.12", "0.0")
      )
    )

    assertAll {
      assertThat(quote.metadata.id.prefix).isEqualTo("quote")
      assertThat(quote.data.payin.amount).isEqualTo("10.00")
    }
  }

  @Test
  fun `can parse an quote from a json string`() {
    val quote = TestData.getQuote()
    quote.sign(TestData.PFI_DID)
    val jsonMessage = quote.toString()
    val parsedMessage = Message.parse(jsonMessage)

    assertIs<Quote>(parsedMessage)
    assertThat(parsedMessage.toString()).isEqualTo(jsonMessage)
  }

  @Test
  fun `can validate a quote`() {
    val quote = TestData.getQuote()
    quote.sign(TestData.PFI_DID)

    assertDoesNotThrow { Message.parse(Json.stringify(quote)) }
  }
}


