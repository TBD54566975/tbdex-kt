package tbdex.sdk.httpserver.handlers

import ServerTest
import TestData
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import tbdex.sdk.httpclient.RequestToken
import tbdex.sdk.httpserver.models.ErrorResponse
import tbdex.sdk.httpserver.models.ExchangesApi
import tbdex.sdk.httpserver.models.GetExchangesCallback
import tbdex.sdk.httpserver.models.GetExchangesFilter
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.serialization.Json
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.methods.dht.DidDht
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class GetExchangesTest {

  @Nested
  inner class GetExchangesServerTest : ServerTest() {

    @BeforeEach
    fun `setup client`() {
      client.config {
        install(ContentNegotiation) {
          jackson {
            registerModule(JavaTimeModule())
            registerKotlinModule()
              .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            findAndRegisterModules()
          }
        }
        install(Auth) {
          bearer {
            sendWithoutRequest { true }
          }
        }
      }
    }

    @Test
    fun `returns 401 if no Bearer token is present`() = runBlocking {

      val response = client.get("/exchanges")

      assertEquals(HttpStatusCode.Unauthorized, response.status)

      val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)
      assertContains(errorResponse.errors.first().detail, "Authorization header required")
    }

    @Test
    fun `returns 401 if malformed Bearer token is present`() = runBlocking {

      val response = client.get("/exchanges") {
        bearerAuth("Bearer abc123")
      }

      assertEquals(HttpStatusCode.Unauthorized, response.status)

      val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)
      assertContains(errorResponse.errors.first().detail, "Malformed Authorization header.")
    }

    @Test
    fun `returns 200 if exchanges are found`() = runBlocking {
      val rfq = TestData.createRfq()
      rfq.sign(TestData.aliceDid)
      val quote = TestData.createQuote()
      quote.sign(TestData.pfiDid)

      exchangesApi.addMessage(rfq)
      exchangesApi.addMessage(quote)

      val response = client.get("/exchanges?id=${rfq.metadata.exchangeId}&id=${quote.metadata.exchangeId}") {
        bearerAuth(RequestToken.generate(TestData.aliceDid, TestData.pfiDid.uri))
      }

      assertEquals(HttpStatusCode.OK, response.status)

      val responseString = response.bodyAsText()
      val jsonNode = Json.jsonMapper.readTree(responseString)
      val exchanges = jsonNode.get("data").elements().asSequence()
        .map { exchange ->
          exchange.elements().asSequence().map {
            val string = it.toString()
            Message.parse(string)
          }.toList()
        }
        .toList()

      assertEquals(2, exchanges.size)
      assertEquals(1, exchanges[0].size)

      assertEquals(rfq.metadata.id, exchanges[0][0].metadata.id)
      assertEquals(quote.metadata.id, exchanges[1][0].metadata.id)
    }
  }

  @Nested
  inner class GetExchangesMockkTest {
    private lateinit var applicationCall: ApplicationCall
    private lateinit var exchangesApi: ExchangesApi
    private val callback: GetExchangesCallback = mockk(relaxed = true)
    private val aliceDid = DidDht.create(InMemoryKeyManager())

    @BeforeEach
    fun setUp() {
      applicationCall = mockk(relaxed = true)
      exchangesApi = mockk(relaxed = true)
    }

    @Test
    fun `verify callback is invoked upon successful get exchange`() = runBlocking {
      val messageList = listOf<Message>(mockk(relaxed = true))
      coEvery { applicationCall.request.headers[HttpHeaders.Authorization] } returns
        "Bearer ${
          RequestToken.generate(
            aliceDid,
            "did:ex:pfi"
          )
        }"

      coEvery { exchangesApi.getExchanges(any<GetExchangesFilter>()) } returns listOf(messageList)

      getExchanges(applicationCall, exchangesApi, callback, "did:ex:pfi")

      coVerify(exactly = 1) { callback.invoke(applicationCall, any<GetExchangesFilter>()) }
      coVerify { applicationCall.respond(HttpStatusCode.OK, any<GetExchangesResponse>()) }

    }

    @Test
    fun `verify http ok is returned if callback is null`() = runBlocking {
      val messageList = listOf<Message>(mockk(relaxed = true))
      coEvery { applicationCall.request.headers[HttpHeaders.Authorization] } returns
        "Bearer ${
          RequestToken.generate(
            aliceDid,
            "did:ex:pfi"
          )
        }"

      coEvery { exchangesApi.getExchanges(any<GetExchangesFilter>()) } returns listOf(messageList)

      getExchanges(applicationCall, exchangesApi, null, "did:ex:pfi")

      coVerify { applicationCall.respond(HttpStatusCode.OK, any<GetExchangesResponse>()) }

    }

    @Test
    fun `verify callback fails if bearer token is absent`() = runBlocking {
      coEvery { applicationCall.request.headers[HttpHeaders.Authorization] } returns null

      getExchanges(applicationCall, exchangesApi, callback, "did:ex:pfi")

      coVerify(exactly = 0) { callback.invoke(applicationCall, any<GetExchangesFilter>()) }
      coVerify {
        applicationCall.respond(
          HttpStatusCode.Unauthorized, any<tbdex.sdk.httpserver.models.ErrorResponse>()
        )
      }

    }

    @Test
    fun `verify callback fails if bearer token is malformatted`() = runBlocking {
      coEvery { applicationCall.request.headers[HttpHeaders.Authorization] } returns
        "Bearer Bearer ${
          RequestToken.generate(
            DidDht.create(InMemoryKeyManager()),
            "did:ex:pfi"
          )
        }"

      getExchanges(applicationCall, exchangesApi, callback, "did:ex:pfi")

      coVerify(exactly = 0) { callback.invoke(applicationCall, any<GetExchangesFilter>()) }
      coVerify {
        applicationCall.respond(
          HttpStatusCode.Unauthorized,
          any<tbdex.sdk.httpserver.models.ErrorResponse>()
        )
      }

    }

    @Test
    fun `verify callback fails if bearer token fails verification`() = runBlocking {
      coEvery { applicationCall.request.headers[HttpHeaders.Authorization] } returns "Bearer invalid_token"

      getExchanges(applicationCall, exchangesApi, callback, "did:ex:pfi")

      coVerify(exactly = 0) { callback.invoke(applicationCall, any<GetExchangesFilter>()) }
      coVerify {
        applicationCall.respond(
          HttpStatusCode.Unauthorized,
          any<tbdex.sdk.httpserver.models.ErrorResponse>()
        )
      }

    }
  }
}