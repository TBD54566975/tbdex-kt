
import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.CloseData
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.PaymentInstruction
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.QuoteData
import tbdex.sdk.protocol.models.QuoteDetails
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.models.RfqData
import tbdex.sdk.protocol.models.SelectedPaymentMethod
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.methods.dht.DidDht
import java.time.OffsetDateTime

object TestData {

  val aliceDid = DidDht.create(InMemoryKeyManager())
  val pfiDid = DidDht.create(InMemoryKeyManager())

  fun createRfq(offering: Offering? = null, claims: List<String>? = emptyList()): Rfq {
    return Rfq.create(
      to = pfiDid.uri,
      from = aliceDid.uri,
      rfqData = RfqData(
        offeringId = offering?.metadata?.id ?: TypeId.generate("offering"),
        payinAmount = "1.00",
        payinMethod = SelectedPaymentMethod(
          kind = offering?.data?.payinMethods?.first()?.kind ?: "USD",
          paymentDetails = mapOf("foo" to "bar")
        ),
        payoutMethod = SelectedPaymentMethod(
          kind = offering?.data?.payoutMethods?.first()?.kind ?: "BTC",
          paymentDetails = mapOf("foo" to "bar")
        ),
        claims = claims ?: emptyList()
      )
    )
  }

  fun createOrder(exchangeId: TypeId) = Order.create(
    to = pfiDid.uri,
    from = aliceDid.uri,
    exchangeId = exchangeId
  )

  fun createClose(exchangeId: TypeId) = Close.create(
    to = pfiDid.uri,
    from = aliceDid.uri,
    exchangeId = exchangeId,
    closeData = CloseData(reason = "test close reason")
  )

  fun createQuote(
    exchangeId: TypeId = TypeId.generate(MessageKind.rfq.name),
    expiresAt: OffsetDateTime = OffsetDateTime.now().plusDays(1)
  ) = Quote.create(
    to = aliceDid.uri,
    from = pfiDid.uri,
    exchangeId = exchangeId,
    quoteData = QuoteData(
      expiresAt = expiresAt,
      payin = QuoteDetails("AUD", "10.00", "0.1", PaymentInstruction(
        link = "https://block.xyz",
        instruction = "payin instruction"
      )),
      payout = QuoteDetails("BTC", "0.12", "0.02", PaymentInstruction(
        link = "https://block.xyz",
        instruction = "payout instruction"
      ))
    )
  )
}