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
import tbdex.sdk.httpserver.models.BalancesApi
import tbdex.sdk.httpserver.models.CallbackError
import tbdex.sdk.httpserver.models.ErrorResponse
import tbdex.sdk.httpserver.models.GetBalancesCallback
import tbdex.sdk.protocol.models.Balance
import tbdex.sdk.protocol.models.BalanceData
import tbdex.sdk.protocol.models.Resource
import tbdex.sdk.protocol.serialization.Json
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.methods.dht.DidDht
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class GetBalancesTest {

  @Nested
  inner class GetBalancesServerTest : ServerTest() {

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

      val response = client.get("/balances")

      assertEquals(HttpStatusCode.Unauthorized, response.status)

      val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)
      assertContains(errorResponse.errors.first().detail, "Authorization header required")
    }

    @Test
    fun `returns 401 if malformed Bearer token is present`() = runBlocking {

      val response = client.get("/balances") {
        bearerAuth("Bearer abc123")
      }

      assertEquals(HttpStatusCode.Unauthorized, response.status)

      val errorResponse = Json.jsonMapper.readValue(response.bodyAsText(), ErrorResponse::class.java)
      assertContains(errorResponse.errors.first().detail, "Malformed Authorization header.")
    }

    @Test
    fun `returns 200 if balances are found`() = runBlocking {
      val balance = Balance.create(
        from = TestData.pfiDid.uri,
        data = BalanceData(
          currencyCode = "USD",
          available = "100.00"
        )
      )
      balance.sign(TestData.pfiDid)
      balancesApi.addBalance(balance)
      val response = client.get("/balances") {
        bearerAuth(RequestToken.generate(TestData.aliceDid, TestData.pfiDid.uri))
      }

      assertEquals(HttpStatusCode.OK, response.status)

      val responseString = response.bodyAsText()
      val jsonNode = Json.jsonMapper.readTree(responseString)
      val balances = jsonNode.get("data").elements().asSequence()
        .map { balance ->
          Resource.parse(balance.toString())
        }
        .toList()

      assertEquals(1, balances.size)
    }

  }

  @Nested
  inner class GetBalancesMockkTest {
    private lateinit var applicationCall: ApplicationCall
    private lateinit var balancesApi: BalancesApi
    private val callback: GetBalancesCallback = mockk(relaxed = true)
    private val aliceDid = DidDht.create(InMemoryKeyManager())

    @BeforeEach
    fun setUp() {
      applicationCall = mockk(relaxed = true)
      balancesApi = mockk(relaxed = true)
    }

    @Test
    fun `verify callback is invoked upon successful get balances`() = runBlocking {
      val balances = listOf<Balance>(mockk(relaxed = true))
      coEvery { applicationCall.request.headers[HttpHeaders.Authorization] } returns
        "Bearer ${
          RequestToken.generate(
            aliceDid,
            "did:ex:pfi"
          )
        }"
      coEvery { balancesApi.getBalances(any<String>()) } returns balances

      getBalances(applicationCall, balancesApi, callback, "did:ex:pfi")

      coVerify(exactly = 1) { callback.invoke(applicationCall) }
      coVerify { applicationCall.respond(HttpStatusCode.OK, any<GetBalancesResponse>()) }

    }

    @Test
    fun `verify get balances returns http ok if callback is null`() = runBlocking {
      val balances = listOf<Balance>(mockk(relaxed = true))
      coEvery { applicationCall.request.headers[HttpHeaders.Authorization] } returns
        "Bearer ${
          RequestToken.generate(
            aliceDid,
            "did:ex:pfi"
          )
        }"
      coEvery { balancesApi.getBalances(any<String>()) } returns balances

      getBalances(applicationCall, balancesApi, null, "did:ex:pfi")

      coVerify { applicationCall.respond(HttpStatusCode.OK, any<GetBalancesResponse>()) }

    }


    @Test
    fun `verify callback fails if bearer token is absent`() = runBlocking {
      coEvery { applicationCall.request.headers[HttpHeaders.Authorization] } returns null

      getBalances(applicationCall, balancesApi, null, "did:ex:pfi")

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

      getBalances(applicationCall, balancesApi, null, "did:ex:pfi")

      coVerify(exactly = 0) { callback.invoke(applicationCall) }
      coVerify { applicationCall.respond(HttpStatusCode.Unauthorized, any<ErrorResponse>()) }

    }

    @Test
    fun `verify callback fails if bearer token fails verification`() = runBlocking {
      coEvery { applicationCall.request.headers[HttpHeaders.Authorization] } returns "Bearer invalid_token"

      getBalances(applicationCall, balancesApi, null, "did:ex:pfi")

      coVerify(exactly = 0) { callback.invoke(applicationCall) }
      coVerify { applicationCall.respond(HttpStatusCode.Unauthorized, any<ErrorResponse>()) }

    }

    @Test
    fun `verify 500 returned if callback throws exception`() = runBlocking {
      val balances = listOf<Balance>(mockk(relaxed = true))
      coEvery { applicationCall.request.headers[HttpHeaders.Authorization] } returns
        "Bearer ${
          RequestToken.generate(
            aliceDid,
            "did:ex:pfi"
          )
        }"
      coEvery { callback.invoke(applicationCall) } throws Exception("error")

      coEvery { balancesApi.getBalances(any<String>()) } returns balances

      getBalances(applicationCall, balancesApi, callback, "did:ex:pfi")

      coVerify(exactly = 1) { callback.invoke(applicationCall) }
      coVerify { applicationCall.respond(HttpStatusCode.InternalServerError, any<ErrorResponse>()) }

    }

    @Test
    fun `verify 4xx returned if callback throws exception`() = runBlocking {
      val balances = listOf<Balance>(mockk(relaxed = true))
      coEvery { applicationCall.request.headers[HttpHeaders.Authorization] } returns
        "Bearer ${
          RequestToken.generate(
            aliceDid,
            "did:ex:pfi"
          )
        }"
      coEvery { callback.invoke(applicationCall) } throws CallbackError(HttpStatusCode.BadRequest, listOf())

      coEvery { balancesApi.getBalances(any<String>()) } returns balances

      getBalances(applicationCall, balancesApi, callback, "did:ex:pfi")

      coVerify(exactly = 1) { callback.invoke(applicationCall) }
      coVerify { applicationCall.respond(HttpStatusCode.BadRequest, any<ErrorResponse>()) }

    }
  }


}