package tbdex.sdk.httpserver.models

import tbdex.sdk.protocol.models.Offering

class FakeOfferingsApi : OfferingsApi {
  override fun getOffering(id: String?): Offering? {
    TODO("Not yet implemented")
  }

  override fun getOfferings(filter: GetOfferingsFilter?): List<Offering>? {
    TODO("Not yet implemented")
  }
}