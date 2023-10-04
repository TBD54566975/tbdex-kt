package protocol

import models.Close
import models.CloseData
import models.CurrencyDetails
import models.MessageKind
import models.Offering
import models.OfferingData
import models.Order
import models.OrderStatus
import models.OrderStatusData
import models.PresentationExchange
import models.Quote
import models.QuoteData
import models.QuoteDetails
import models.ResourceKind
import models.Rfq
import models.RfqData
import models.SelectedPaymentMethod
import typeid.TypeID
import java.time.OffsetDateTime

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

  fun getQuote() = Quote.create(
    alice, pfi, TypeID(MessageKind.rfq.name),
    QuoteData(
      expiresAt = OffsetDateTime.now().plusDays(1),
      payin = QuoteDetails("AUD", 10_00, 0),
      payout = QuoteDetails("BTC", 12, 0)
    )
  )

  fun getClose() = Close.create(alice, pfi, TypeID(MessageKind.rfq.name), CloseData("test reason"))

  fun getOrder() = Order.create(pfi, alice, TypeID(MessageKind.rfq.name))

  fun getOrderStatus() = OrderStatus.create(
    alice, pfi, TypeID(MessageKind.rfq.name), OrderStatusData("test status")
  )
}
