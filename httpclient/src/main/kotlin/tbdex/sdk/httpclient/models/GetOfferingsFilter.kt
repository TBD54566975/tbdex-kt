package tbdex.sdk.httpclient.models

/**
 * Get offerings filter
 *
 * @property payinCurrency
 * @property payoutCurrency
 * @property id
 * @constructor Create empty Get offerings filter
 */
class GetOfferingsFilter(
  val payinCurrency: String? = null,
  val payoutCurrency: String? = null,
  // todo what is this id? OfferingId?
  val id: String? = null
)
