package tbdex.sdk.httpserver.handlers

import ServerTest
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class GetOfferingsTest : ServerTest() {

  @Test
  fun `returns 200 if offerings are found`() = runBlocking {
    val response = client.get("/offerings")

    val offeringsResponse = Json.jsonMapper.readValue(response.bodyAsText(), GetOfferingsResponse::class.java)
    assertEquals(HttpStatusCode.OK, response.status)
    assertEquals(offeringsResponse.data?.get(0)?.metadata?.from, "did:ex:pfi")
  }

}