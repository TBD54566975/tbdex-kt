package tbdex.sdk.httpclient

import tbdex.sdk.httpclient.models.GetExchangesFilter
import tbdex.sdk.httpclient.models.GetOfferingsFilter
import tbdex.sdk.httpclient.models.TbdexResponse
import tbdex.sdk.protocol.models.Message
import web5.sdk.dids.Did

interface TbdexClient {
  fun getOfferings(pfiDid: String, filter: GetOfferingsFilter? = null): TbdexResponse
  fun sendMessage(message: Message): TbdexResponse
  fun getExchange(pfiDid: String, exchangeId: String, did: Did): TbdexResponse
  fun getExchanges(pfiDid: String, did: Did, filter: GetExchangesFilter? = null): TbdexResponse
}