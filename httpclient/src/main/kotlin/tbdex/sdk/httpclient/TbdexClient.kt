package tbdex.sdk.httpclient

import tbdex.sdk.httpclient.models.GetExchangesFilter
import tbdex.sdk.httpclient.models.GetOfferingsFilter
import tbdex.sdk.httpclient.models.TbdexResponse
import tbdex.sdk.protocol.models.Message
import web5.sdk.dids.Did

/**
 * Tbdex client
 *
 */
interface TbdexClient {
  /**
   * Get offerings
   *
   * @param pfiDid
   * @param filter
   * @return
   */
  fun getOfferings(pfiDid: String, filter: GetOfferingsFilter? = null): TbdexResponse

  /**
   * Send message
   *
   * @param message
   * @return
   */
  fun sendMessage(message: Message): TbdexResponse

  /**
   * Get exchange
   *
   * @param pfiDid
   * @param exchangeId
   * @param did
   * @return
   */
  fun getExchange(pfiDid: String, exchangeId: String, did: Did): TbdexResponse

  /**
   * Get exchanges
   *
   * @param pfiDid
   * @param did
   * @param filter
   * @return
   */
  fun getExchanges(pfiDid: String, did: Did, filter: GetExchangesFilter? = null): TbdexResponse
}