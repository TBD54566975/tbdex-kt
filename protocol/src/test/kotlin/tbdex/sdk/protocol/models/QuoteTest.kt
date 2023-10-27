package tbdex.sdk.protocol.models

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.assertDoesNotThrow
import tbdex.sdk.protocol.TestData
import tbdex.sdk.protocol.serialization.Json
import typeid.TypeID
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertIs

class QuoteTest {
  @Test
  fun `can create a new quote`() {
    val quote = Quote.create(
      "pfi", "alice", TypeID(MessageKind.rfq.name),
      QuoteData(
        expiresAt = OffsetDateTime.now().plusDays(1),
        payin = QuoteDetails("AUD", "1000", "0"),
        payout = QuoteDetails("BTC", "12", "0")
      )
    )

    assertAll {
      assertThat(quote.metadata.id.prefix).isEqualTo("quote")
      assertThat(quote.data.payin.amountSubunits).isEqualTo("1000")
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


