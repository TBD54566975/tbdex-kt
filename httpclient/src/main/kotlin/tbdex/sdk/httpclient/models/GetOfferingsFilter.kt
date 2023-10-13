package tbdex.sdk.httpclient.models

class GetOfferingsFilter(
  val payinCurrency: String? = null,
  val payoutCurrency: String? = null,
  // todo what is this id? OfferingId?
  val id: String? = null
)
