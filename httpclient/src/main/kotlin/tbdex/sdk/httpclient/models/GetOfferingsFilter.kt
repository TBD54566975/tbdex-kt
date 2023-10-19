package tbdex.sdk.httpclient.models

/**
 * A filter object used to query offerings.
 *
 * @property payinCurrency The currency used for payments (optional).
 * @property payoutCurrency The currency used for payouts (optional).
 * @property id The unique identifier of the offering (optional).
 */
class GetOfferingsFilter(
  val payinCurrency: String? = null,
  val payoutCurrency: String? = null,
  val id: String? = null
)
