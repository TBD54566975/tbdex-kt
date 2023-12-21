import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.models.*
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.methods.dht.DidDht
import java.time.OffsetDateTime

object TestData {

  val aliceDid = DidDht.create(InMemoryKeyManager())
  val pfiDid = DidDht.create(InMemoryKeyManager())

  fun createRfq(offering: Offering? = null): Rfq {
    return Rfq.create(
      to = pfiDid.uri,
      from = aliceDid.uri,
      rfqData = RfqData(
        offeringId = offering?.metadata?.id ?: TypeId.generate("offering"),
        payinSubunits = "100",
        payinMethod = SelectedPaymentMethod(
          kind = offering?.data?.payinMethods?.first()?.kind ?: "USD",
          paymentDetails = mapOf("foo" to "bar")
        ),
        payoutMethod = SelectedPaymentMethod(
          kind = offering?.data?.payoutMethods?.first()?.kind ?: "BTC",
          paymentDetails = mapOf("foo" to "bar")
        ),
        claims = listOf("foo")
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
      payin = QuoteDetails("AUD", "1000", "1"),
      payout = QuoteDetails("BTC", "12", "2"),
      paymentInstructions = PaymentInstructions(
        payin = PaymentInstruction(
          link = "https://block.xyz",
          instruction = "payin instruction"
        ),
        payout = PaymentInstruction(
          link = "https://block.xyz",
          instruction = "payout instruction"
        )
      )
    )
  )
}