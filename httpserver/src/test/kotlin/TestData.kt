import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.models.*
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.methods.dht.DidDht

object TestData {

  val aliceDid = DidDht.create(InMemoryKeyManager())
  val pfiDid = DidDht.create(InMemoryKeyManager())

  fun createRfq() = Rfq.create(
    to = pfiDid.uri,
    from = aliceDid.uri,
    rfqData = RfqData(
      offeringId = TypeId.generate("offering"),
      payinSubunits = "100",
      payinMethod = SelectedPaymentMethod(
        kind = "USD",
        paymentDetails = mapOf("foo" to "bar")
      ),
      payoutMethod = SelectedPaymentMethod(
        kind = "BTC",
        paymentDetails = mapOf("foo" to "bar")
      ),
      claims = listOf("foo")
    )
  )

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
}