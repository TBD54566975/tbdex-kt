package tbdex.sdk.httpserver.handlers

import ServerTest
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import tbdex.sdk.httpserver.models.CallbackError
import tbdex.sdk.httpserver.models.ErrorResponse
import tbdex.sdk.httpserver.models.GetOfferingsCallback
import tbdex.sdk.httpserver.models.OfferingsApi
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class GetOfferingsTest {

  @Nested
  inner class GetOfferingsServerTest : ServerTest() {

    @Test
    fun `returns 200 if offerings are found`() = runBlocking {
      val response = client.get("/offerings")

      val offeringsResponse = Json.jsonMapper.readValue(response.bodyAsText(), GetOfferingsResponse::class.java)
      assertEquals(HttpStatusCode.OK, response.status)
      assertEquals(offeringsResponse.data?.get(0)?.metadata?.from, "did:ex:pfi")
    }
  }

  @Nested
  inner class GetOfferingsMockkTest {
    private lateinit var applicationCall: ApplicationCall
    private lateinit var offeringsApi: OfferingsApi
    private val callback: GetOfferingsCallback = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
      applicationCall = mockk(relaxed = true)
      offeringsApi = mockk(relaxed = true)
    }

    @Test
    fun `verify callback is invoked upon successful get offerings`() = runBlocking {
      val offerings = listOf<Offering>(mockk(relaxed = true))

      coEvery { offeringsApi.getOfferings() } returns offerings

      getOfferings(applicationCall, offeringsApi, callback)

      coVerify(exactly = 1) { callback.invoke(applicationCall) }
      coVerify { applicationCall.respond(HttpStatusCode.OK, any<GetOfferingsResponse>()) }

    }

    @Test
    fun `verify get offerings returns http ok if callback is null`() = runBlocking {
      val offerings = listOf<Offering>(mockk(relaxed = true))

      coEvery { offeringsApi.getOfferings() } returns offerings

      getOfferings(applicationCall, offeringsApi, null)

      coVerify { applicationCall.respond(HttpStatusCode.OK, any<GetOfferingsResponse>()) }

    }

    @Test
    fun `verify 500 returned if callback throws exception`() = runBlocking {
      val offerings = listOf<Offering>(mockk(relaxed = true))
      coEvery { callback.invoke(applicationCall) } throws Exception("error")

      coEvery { offeringsApi.getOfferings() } returns offerings

      getOfferings(applicationCall, offeringsApi, callback)

      coVerify(exactly = 1) { callback.invoke(applicationCall) }
      coVerify { applicationCall.respond(HttpStatusCode.InternalServerError, any<ErrorResponse>()) }

    }

    @Test
    fun `verify 4xx returned if callback throws exception`() = runBlocking {
      val offerings = listOf<Offering>(mockk(relaxed = true))
      coEvery { callback.invoke(applicationCall) } throws CallbackError(HttpStatusCode.BadRequest, listOf())

      coEvery { offeringsApi.getOfferings() } returns offerings

      getOfferings(applicationCall, offeringsApi, callback)

      coVerify(exactly = 1) { callback.invoke(applicationCall) }
      coVerify { applicationCall.respond(HttpStatusCode.BadRequest, any<ErrorResponse>()) }

    }
  }


}