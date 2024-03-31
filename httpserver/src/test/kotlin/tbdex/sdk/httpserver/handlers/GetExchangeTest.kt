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
import tbdex.sdk.httpserver.models.GetExchangeCallback
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.serialization.Json
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.methods.dht.DidDht
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class GetExchangeTest {

  @Nested
  inner class GetExchangeServerTest : ServerTest() {

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

      val response = client.get("/exchanges/123")

      assertEquals(HttpStatusCode.Unauthorized, response.status)

      val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)
      assertContains(errorResponse.errors.first().detail, "Authorization header required")
    }

    @Test
    fun `returns 401 if malformed Bearer token is present`() = runBlocking {

      val response = client.get("/exchanges/123") {
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

      exchangesApi.addMessage(rfq)

      val response = client.get("/exchanges/${rfq.metadata.exchangeId}") {
        bearerAuth(RequestToken.generate(TestData.aliceDid, TestData.pfiDid.uri))
      }

      assertEquals(HttpStatusCode.OK, response.status)

      val responseString = response.bodyAsText()
      val jsonNode = Json.jsonMapper.readTree(responseString)
      val exchange = jsonNode.get("data").elements().asSequence()
        .map {
          val string = it.toString()
          Message.parse(string)
        }.toList()

      assertEquals((exchange[0] as Rfq).metadata.from, TestData.aliceDid.uri)
    }
  }

  @Nested
  inner class GetExchangeMockkTest {
    private lateinit var applicationCall: ApplicationCall
    private lateinit var exchangesApi: ExchangesApi
    private val callback: GetExchangeCallback = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
      applicationCall = mockk(relaxed = true)
      exchangesApi = mockk(relaxed = true)
    }

    @Test
    fun `verify callback is invoked upon successful get exchange`() = runBlocking {
      val messageList = listOf<Message>(mockk(relaxed = true))
      val aliceDid = DidDht.create(InMemoryKeyManager())
      coEvery { applicationCall.request.headers[HttpHeaders.Authorization] } returns
        "Bearer ${
          RequestToken.generate(
            aliceDid,
            "did:ex:pfi"
          )
        }"

      coEvery { exchangesApi.getExchange(any<String>(), any<String>()) } returns messageList

      getExchange(applicationCall, exchangesApi, callback, "did:ex:pfi")

      coVerify(exactly = 1) { callback.invoke(applicationCall) }
      coVerify { applicationCall.respond(HttpStatusCode.OK, any<GetExchangeResponse>()) }

    }

    @Test
    fun `verify http ok is returned if callback is null`() = runBlocking {
      val messageList = listOf<Message>(mockk(relaxed = true))
      val aliceDid = DidDht.create(InMemoryKeyManager())
      coEvery { applicationCall.request.headers[HttpHeaders.Authorization] } returns
        "Bearer ${
          RequestToken.generate(
            aliceDid,
            "did:ex:pfi"
          )
        }"

      coEvery { exchangesApi.getExchange(any<String>(), any<String>()) } returns messageList

      getExchange(applicationCall, exchangesApi, null, "did:ex:pfi")

      coVerify { applicationCall.respond(HttpStatusCode.OK, any<GetExchangeResponse>()) }

    }

    @Test
    fun `verify callback fails if bearer token is absent`() = runBlocking {
      coEvery { applicationCall.request.headers[HttpHeaders.Authorization] } returns null

      getExchange(applicationCall, exchangesApi, callback, "did:ex:pfi")

      coVerify(exactly = 0) { callback.invoke(applicationCall) }
      coVerify { applicationCall.respond(HttpStatusCode.Unauthorized, any<ErrorResponse>()) }

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

      getExchange(applicationCall, exchangesApi, callback, "did:ex:pfi")

      coVerify(exactly = 0) { callback.invoke(applicationCall) }
      coVerify { applicationCall.respond(HttpStatusCode.Unauthorized, any<ErrorResponse>()) }

    }

    @Test
    fun `verify callback fails if bearer token fails verification`() = runBlocking {
      coEvery { applicationCall.request.headers[HttpHeaders.Authorization] } returns "Bearer invalid_token"

      getExchange(applicationCall, exchangesApi, callback, "did:ex:pfi")

      coVerify(exactly = 0) { callback.invoke(applicationCall) }
      coVerify { applicationCall.respond(HttpStatusCode.Unauthorized, any<ErrorResponse>()) }

    }

  }
}