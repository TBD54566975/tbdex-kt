package tbdex.sdk.httpclient

import okhttp3.Headers
import protocol.tbdex.sdk.protocol.TestData
import tbdex.sdk.httpclient.models.GetExchangesFilter
import tbdex.sdk.httpclient.models.GetOfferingsFilter
import tbdex.sdk.httpclient.models.GetOfferingsResponse
import tbdex.sdk.httpclient.models.TbdexResponse
import tbdex.sdk.protocol.models.Message
import web5.sdk.dids.Did

object FakeTbdexClient : TbdexClient {
  override fun getOfferings(pfiDid: String, filter: GetOfferingsFilter?): TbdexResponse {
    val offerings = listOf(TestData.getOffering())

    return GetOfferingsResponse(
      status = 200,
      headers = Headers.Builder().add("foo", "bar").build(),
      data = offerings
    )
  }

  override fun sendMessage(message: Message): TbdexResponse {
    TODO("Not yet implemented")
  }

  override fun getExchange(pfiDid: String, exchangeId: String, did: Did): TbdexResponse {
    TODO("Not yet implemented")
  }

  override fun getExchanges(pfiDid: String, did: Did, filter: GetExchangesFilter?): TbdexResponse {
    TODO("Not yet implemented")
  }
}