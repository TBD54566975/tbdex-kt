package tbdex.sdk.protocol

import com.danubetech.verifiablecredentials.CredentialSubject
import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.models.Balance
import tbdex.sdk.protocol.models.BalanceData
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.CloseData
import tbdex.sdk.protocol.models.CreateRfqData
import tbdex.sdk.protocol.models.CreateSelectedPayinMethod
import tbdex.sdk.protocol.models.CreateSelectedPayoutMethod
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.OfferingData
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.OrderStatus
import tbdex.sdk.protocol.models.OrderStatusData
import tbdex.sdk.protocol.models.PayinDetails
import tbdex.sdk.protocol.models.PaymentInstruction
import tbdex.sdk.protocol.models.PayoutDetails
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.QuoteData
import tbdex.sdk.protocol.models.QuoteDetails
import tbdex.sdk.protocol.models.ResourceKind
import tbdex.sdk.protocol.models.Rfq
import web5.sdk.credentials.VcDataModel
import web5.sdk.credentials.VerifiableCredential
import web5.sdk.credentials.model.ConstraintsV2
import web5.sdk.credentials.model.FieldV2
import web5.sdk.credentials.model.InputDescriptorV2
import web5.sdk.credentials.model.PresentationDefinitionV2
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.did.BearerDid
import web5.sdk.dids.methods.key.DidKey
import java.net.URI
import java.time.OffsetDateTime
import java.util.Date
import java.util.UUID

object TestData {
  const val ALICE = "alice"
  const val PFI = "pfi"
  private val aliceKeyManager = InMemoryKeyManager()
  private val pfiKeyManager = InMemoryKeyManager()
  val ALICE_DID: BearerDid = DidKey.create(aliceKeyManager)
  val PFI_DID: BearerDid = DidKey.create(pfiKeyManager)

  fun getPresentationDefinition(): PresentationDefinitionV2 {
    return buildPresentationDefinition(
      inputDescriptors = listOf(
        buildInputDescriptor(fields = listOf(buildField(paths = arrayOf("$.credentialSubject.btcAddress"))))
      )
    )
  }

  fun getVC(): VerifiableCredential {
    val credentialSubject = CredentialSubject.builder()
      .id(URI.create(ALICE_DID.uri))
      .claims(mutableMapOf<String, Any>().apply { this["btcAddress"] = "btcAddress123" })
      .build()

    val vc = VcDataModel.builder()
      .id(URI.create(UUID.randomUUID().toString()))
      .credentialSubject(credentialSubject)
      .issuer(URI.create(ALICE_DID.uri))
      .issuanceDate(Date())
      .build()

    return VerifiableCredential.create("test type", ALICE_DID.uri, ALICE_DID.uri, vc)
  }

  fun getOffering(from: String = PFI_DID.uri, requiredClaims: PresentationDefinitionV2 = getPresentationDefinition()) =
    Offering.create(
      from = from,
      OfferingData(
        description = "A sample offering",
        payoutUnitsPerPayinUnit = "1",
        payin = PayinDetails("BTC", "0.00", "100.00", listOf()),
        payout = PayoutDetails("USDC", "0.00", "100.00", listOf()),
        requiredClaims = requiredClaims
      )
    )

  fun getBalance() =
    Balance.create(
      from = PFI_DID.uri,
      data = BalanceData(
        currencyCode = "BTC",
        available = "0.001",
      )
    )

  fun getRfq(
    offeringId: String = TypeId.generate(ResourceKind.offering.name).toString(),
    claims: List<String> = emptyList()
  ) = Rfq.create(
    to = PFI_DID.uri,
    from = ALICE_DID.uri,
    rfqData = CreateRfqData(
      offeringId = offeringId,
      payin = CreateSelectedPayinMethod("BTC_ADDRESS", mapOf("address" to "123456"), amount = "10.00"),
      payout = CreateSelectedPayoutMethod(
        "MOMO", mapOf(
        "phoneNumber" to "+254712345678",
        "accountHolderName" to "Alfred Holder"
      )
      ),
      claims = claims
    )
  )

  fun getQuote() = Quote.create(
    ALICE_DID.uri, PFI_DID.uri, TypeId.generate(MessageKind.rfq.name).toString(),
    QuoteData(
      expiresAt = OffsetDateTime.now().plusDays(1),
      payin = QuoteDetails(
        currencyCode = "AUD",
        amount = "10.00",
        fee = "0.01",
        paymentInstruction =  PaymentInstruction(
        link = "https://block.xyz",
        instruction = "payin instruction"
      )
      ),
      payout = QuoteDetails(
        currencyCode = "BTC",
        amount = "0.12",
        fee = "0.02",
        paymentInstruction = PaymentInstruction(
        link = "https://block.xyz",
        instruction = "payout instruction"
      )
      ),
    )
  )

  fun getClose() = Close.create(
    to = ALICE_DID.uri,
    from = PFI_DID.uri,
    exchangeId = TypeId.generate(MessageKind.rfq.name).toString(),
    closeData = CloseData("test reason")
  )

  fun getOrder() = Order.create(
    to = PFI_DID.uri,
    from = ALICE_DID.uri,
    exchangeId = TypeId.generate(MessageKind.rfq.name).toString()
  )

  fun getOrderStatus() = OrderStatus.create(
    to = ALICE_DID.uri,
    from = PFI_DID.uri,
    exchangeId = TypeId.generate(MessageKind.rfq.name).toString(),
    orderStatusData = OrderStatusData("PENDING")
  )

  fun getOrderStatusWithInvalidDid(): OrderStatus {
    val os = OrderStatus.create(
      "alice", "pfi", TypeId.generate(MessageKind.rfq.name).toString(), OrderStatusData("PENDING")
    )

    os.sign(ALICE_DID)
    return os
  }

  private fun buildField(id: String? = null, vararg paths: String): FieldV2 {
    return FieldV2(id = id, path = paths.toList())
  }

  private fun buildPresentationDefinition(
    id: String = "test-pd-id",
    name: String = "simple PD",
    purpose: String = "pd for testing",
    inputDescriptors: List<InputDescriptorV2> = listOf()
  ): PresentationDefinitionV2 {
    return PresentationDefinitionV2(
      id = id,
      name = name,
      purpose = purpose,
      inputDescriptors = inputDescriptors
    )
  }

  private fun buildInputDescriptor(
    id: String = "whatever",
    purpose: String = "id for testing",
    fields: List<FieldV2> = listOf()
  ): InputDescriptorV2 {
    return InputDescriptorV2(
      id = id,
      purpose = purpose,
      constraints = ConstraintsV2(fields = fields)
    )
  }
}
