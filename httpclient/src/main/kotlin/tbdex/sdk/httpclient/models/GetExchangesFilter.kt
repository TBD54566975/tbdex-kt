package tbdex.sdk.httpclient.models

/**
 * A filter object used to query exchanges.
 *
 * @property exchangeIds The list of exchange IDs to filter by.
 */
class GetExchangesFilter(
  val exchangeIds: List<String>
)
