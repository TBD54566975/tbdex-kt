package protocol.models

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import models.Message
import models.MessageKind
import models.Quote
import models.QuoteData
import models.QuoteDetails
import protocol.TestData
import typeid.TypeID
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertIs

class QuoteTest {
  @Test
  fun `can create a new quote`() {
    val quote = Quote.create(
      "pfi", "alice", TypeID(MessageKind.rfq.name),
      QuoteData(
        expiresAt = OffsetDateTime.now().plusDays(1),
        payin = QuoteDetails("AUD", 10_00, 0),
        payout = QuoteDetails("BTC", 12, 0)
      )
    )

    assertAll {
      assertContains(quote.metadata.id.toString(), "quote")
      assertThat(quote.data.payin.amountSubunits).isEqualTo(10_00)
    }
  }

  @Test
  fun `sign populates quote signature`() {
    val quote = TestData.getQuote()
    quote.sign("fakepk", "fakekid")

    assertThat(quote.signature).isEqualTo("blah")
  }

  @Test
  fun `can parse an quote from a json string`() {
    val quote = TestData.getQuote()
    quote.sign("fakepk", "fakekid")
    val jsonMessage = quote.toJson()
    val parsedMessage = Message.parse(jsonMessage)

    assertIs<Quote>(parsedMessage)
    assertThat(parsedMessage.toJson()).isEqualTo(jsonMessage)
  }

  @Test
  fun `can validate a quote`() {
    val quote = TestData.getQuote()
    quote.sign("fakepk", "fakekid")

    try {
      Message.validate(Json.stringify(quote))
    } catch (e: Exception) {
      throw e
    }
  }
}


