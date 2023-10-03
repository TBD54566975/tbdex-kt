package website.tbd.tbdex.protocol

import typeid.TypeID
import website.tbd.tbdex.protocol.message_kinds.Rfq
import website.tbd.tbdex.protocol.message_kinds.RfqData
import website.tbd.tbdex.protocol.message_kinds.SelectedPaymentMethod
import website.tbd.tbdex.protocol.resource_kinds.CurrencyDetails
import website.tbd.tbdex.protocol.resource_kinds.Offering
import website.tbd.tbdex.protocol.resource_kinds.OfferingData
import website.tbd.tbdex.protocol.resource_kinds.PresentationExchange

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
