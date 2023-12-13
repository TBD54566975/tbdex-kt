package tbdex.sdk.httpserver.models

import tbdex.sdk.protocol.models.Offering

interface OfferingsApi {
  fun getOffering(id: String? = null): Offering?
  fun getOfferings(filter: GetOfferingsFilter? = null): List<Offering>?
}

