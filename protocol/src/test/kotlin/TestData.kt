package protocol

import models.CurrencyDetails
import models.Offering
import models.OfferingData
import models.PresentationExchange
import models.ResourceKind
import models.Rfq
import models.RfqData
import models.SelectedPaymentMethod
import typeid.TypeID

object TestData {
  val alice = "alice"
  val pfi = "pfi"

  fun getOffering() = Offering.create(
    from = pfi,
    OfferingData(
      description = "my fake offering",
      payoutUnitsPerPayinUnit = 1,
      payinCurrency = CurrencyDetails("", "", ""),
      payoutCurrency = CurrencyDetails("", "", ""),
      payinMethods = listOf(),
      payoutMethods = listOf(),
      requiredClaims = PresentationExchange()
    )
  )

  fun getRfq(offeringId: TypeID = TypeID(ResourceKind.offering.name)) = Rfq.create(
    pfi,
    alice,
    RfqData(
      offeringID = offeringId,
      payinSubunits = 10_00,
      payinMethod = SelectedPaymentMethod("BTC_ADDRESS", mapOf("address" to 123456)),
      payoutMethod = SelectedPaymentMethod("MOMO", mapOf("phone_number" to 123456)),
      claims = emptyList()
    )
  )
}
