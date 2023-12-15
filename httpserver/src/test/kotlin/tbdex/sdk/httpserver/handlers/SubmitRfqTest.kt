package tbdex.sdk.httpserver.handlers

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nimbusds.jose.shaded.gson.Gson
import de.fxlae.typeid.TypeId
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson
import io.ktor.server.testing.testApplication
import org.junit.jupiter.api.Test
import software.amazon.ion.system.IonTextWriterBuilder.json
import tbdex.sdk.httpclient.models.ErrorResponse
import tbdex.sdk.httpserver.TbdexHttpServer
import tbdex.sdk.httpserver.TbdexHttpServerConfig
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.models.RfqData
import tbdex.sdk.protocol.models.SelectedPaymentMethod
import tbdex.sdk.protocol.serialization.TypeIdModule
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.methods.dht.DidDht
import kotlin.test.assertContains
import kotlin.test.assertEquals

class SubmitRfqTest {
  val aliceDid = DidDht.create(InMemoryKeyManager())
  val pfiDid = DidDht.create(InMemoryKeyManager())

  @Test
  fun `returns 400 if no request body is provided`() {
    testApplication {
      application {
        val serverConfig = TbdexHttpServerConfig(
          port = 8080,
        )
        val tbdexServer = TbdexHttpServer(serverConfig)
        tbdexServer.configure(this)
      }

      val client = createClient {
        install(ContentNegotiation) {
          json()
        }
      }

      val response = client.post("/exchanges/123/rfq") {
        contentType(ContentType.Application.Json)
      }

      val errorResponse = Gson().fromJson(response.bodyAsText(), ErrorResponse::class.java)

      assertEquals(HttpStatusCode.BadRequest, response.status)
      assertContains(errorResponse.errors.first().detail, "Parsing of TBDex message failed")
    }
  }

  @Test
  fun `returns 409 if rfq already exists for a given exchangeId`() {
    testApplication {
      application {
        val serverConfig = TbdexHttpServerConfig(
          port = 8080,
        )
        val tbdexServer = TbdexHttpServer(serverConfig)
        tbdexServer.configure(this)
      }

      val client = createClient {
        install(ContentNegotiation) {
          jackson {
            registerModule(JavaTimeModule())
            registerModules(TypeIdModule())
            registerKotlinModule()
            findAndRegisterModules()
          }
        }
      }
      val rfq = createRfq()
      rfq.sign(aliceDid)

      val response = client.post("/exchanges/123/rfq") {
        contentType(ContentType.Application.Json)
        setBody(rfq)
      }

      val errorResponse = Gson().fromJson(response.bodyAsText(), ErrorResponse::class.java)

      assertEquals(HttpStatusCode.Conflict, response.status)
      assertContains(errorResponse.errors.first().detail, "RFQ already exists.")
    }
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