package tbdex.sdk.httpserver.handlers

import ServerTest
import de.fxlae.typeid.TypeId
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import tbdex.sdk.httpclient.models.ErrorResponse
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.models.RfqData
import tbdex.sdk.protocol.models.SelectedPaymentMethod
import tbdex.sdk.protocol.serialization.Json
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.methods.dht.DidDht
import kotlin.test.assertContains
import kotlin.test.assertEquals


class SubmitRfqTest : ServerTest() {
  private val aliceDid = DidDht.create(InMemoryKeyManager())
  private val pfiDid = DidDht.create(InMemoryKeyManager())

  @Test
  fun `returns 400 if no request body is provided`() = runBlocking {
    val response = client.post("/exchanges/123/rfq") {
      contentType(ContentType.Application.Json)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.BadRequest, response.status)
    assertContains(errorResponse.errors.first().detail, "Parsing of TBDex message failed")
  }

  @Test
  fun `returns 409 if rfq already exists for a given exchangeId`() = runBlocking {
    val rfq = createRfq()
    rfq.sign(aliceDid)

    val response = client.post("/exchanges/123/rfq") {
      contentType(ContentType.Application.Json)
      setBody(rfq)
    }

    val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)

    assertEquals(HttpStatusCode.Conflict, response.status)
    assertContains(errorResponse.errors.first().detail, "RFQ already exists.")
  }

  private fun createRfq() = Rfq.create(
    to = pfiDid.uri,
    from = aliceDid.uri,
    rfqData = RfqData(
      offeringId = TypeId.generate("offering"),
      payinSubunits = "100",
      payinMethod = SelectedPaymentMethod(
        kind = "USD",
        paymentDetails = mapOf("foo" to "bar")
      ),
      payoutMethod = SelectedPaymentMethod(
        kind = "BTC",
        paymentDetails = mapOf("foo" to "bar")
      ),
      claims = listOf("foo")
    )
  )

}