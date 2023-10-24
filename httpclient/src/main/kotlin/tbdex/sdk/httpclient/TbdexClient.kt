package tbdex.sdk.httpclient

import tbdex.sdk.httpclient.models.GetExchangesFilter
import tbdex.sdk.httpclient.models.GetOfferingsFilter
import tbdex.sdk.httpclient.models.TbdexResponse
import tbdex.sdk.protocol.models.Message
import web5.sdk.dids.Did

/**
 * An interface representing a client for interacting with the Tbdex API.
 */
interface TbdexClient {

  /**
   * Retrieves a list of offerings from the Tbdex API.
   *
   * @param pfiDid The PFI DID (Decentralized Identifier) for which to retrieve offerings.
   * @param filter An optional filter to narrow down the query. Default is null.
   * @return A [TbdexResponse] containing the result of the request.
   */
  fun getOfferings(pfiDid: String, filter: GetOfferingsFilter? = null): TbdexResponse

  /**
   * Sends a message to the Tbdex API.
   *
   * @param message The [Message] object representing the message to be sent.
   * @return A [TbdexResponse] containing the result of the request.
   */
  fun sendMessage(message: Message): TbdexResponse

  /**
   * Retrieves a specific exchange from the Tbdex API.
   *
   * @param pfiDid The PFI DID (Decentralized Identifier) for which to retrieve the exchange.
   * @param exchangeId The unique identifier of the exchange to retrieve.
   * @param did The [Did] object representing the DID associated with the request.
   * @return A [TbdexResponse] containing the result of the request.
   */
  fun getExchange(pfiDid: String, exchangeId: String, did: Did): TbdexResponse

  /**
   * Retrieves a list of exchanges from the Tbdex API.
   *
   * @param pfiDid The PFI DID (Decentralized Identifier) for which to retrieve exchanges.
   * @param did The [Did] object representing the DID associated with the request.
   * @param filter An optional filter to narrow down the query. Default is null.
   * @return A [TbdexResponse] containing the result of the request.
   */
  fun getExchanges(pfiDid: String, did: Did, filter: GetExchangesFilter? = null): TbdexResponse
}
