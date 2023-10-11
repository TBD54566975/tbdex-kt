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
  val did = "did:ion:EiBFU3435y86IIthWg9OSMCpx-cjtBV0RTGHGPjs6TxQag:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJkd24tc2lnIiwicHVibGljS2V5SndrIjp7ImNydiI6IkVkMjU1MTkiLCJrdHkiOiJPS1AiLCJ4IjoiOU5xN3BObG0xV1BFa2lwcDRpSXNsYTc5RVctNnc5b1NLWWhVZWVuX3lwcyJ9LCJwdXJwb3NlcyI6WyJhdXRoZW50aWNhdGlvbiJdLCJ0eXBlIjoiSnNvbldlYktleTIwMjAifV0sInNlcnZpY2VzIjpbeyJpZCI6InBmaSIsInNlcnZpY2VFbmRwb2ludCI6Imh0dHBzOi8vcGZpLnllbGxvd2NhcmQuZW5naW5lZXJpbmciLCJ0eXBlIjoiUEZJIn1dfX1dLCJ1cGRhdGVDb21taXRtZW50IjoiRWlBQ2cxRWFIaXFjZUQ1N1VBcXZ3bF9uaHplWjZ6MTBacVF0UWV2d0xDelB5dyJ9LCJzdWZmaXhEYXRhIjp7ImRlbHRhSGFzaCI6IkVpQ0tYTDZDRS1hZlNfUUdKbmxNaHdPV0dvNDR0VEtHZTlZQ041QjN1bzZ1M3ciLCJyZWNvdmVyeUNvbW1pdG1lbnQiOiJFaUNJSkFBME11a21Pb1Uxc3JLSjdiOTNDZGVJSy0zMk5GVnl6ZVRzektzYzNRIn19"
  val alice = did
  val pfi = did

  fun getOffering() = Offering.create(
    from = pfi,
    OfferingData(
      description = "my fake offering",
      payoutUnitsPerPayinUnit = 1,
      payinCurrency = CurrencyDetails("AUD"),
      payoutCurrency = CurrencyDetails("USDC"),
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
