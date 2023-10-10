package models

class GetOfferingsOptions(
  val pfiDid: String,
  val filter: GetOfferingsFilter? = null
) {
  class GetOfferingsFilter(
    val payinCurrency: String?,
    val payoutCurrency: String?,
    val id: String?
  )
}