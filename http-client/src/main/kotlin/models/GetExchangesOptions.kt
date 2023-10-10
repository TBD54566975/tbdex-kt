package models

import web5.sdk.dids.Did

class GetExchangesOptions(
  val pfiDid: String,
  val did: Did,
  val filter: GetExchangesFilter?
) {
  class GetExchangesFilter(
    val exchangeIds: List<String>
  )
}


