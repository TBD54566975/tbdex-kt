package protocol.tbdex.sdk.protocol

import com.danubetech.verifiablecredentials.CredentialSubject
import tbdex.sdk.protocol.models.*
import typeid.TypeID
import web5.sdk.credentials.*
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.Did
import web5.sdk.dids.DidKey
import java.net.URI
import java.time.OffsetDateTime
import java.util.*

object TestData {
  const val ALICE = "alice"
  const val PFI = "pfi"
  private val aliceKeyManager = InMemoryKeyManager()
  private val pfiKeyManager = InMemoryKeyManager()
  val ALICE_DID: Did = DidKey.create(aliceKeyManager)
  val PFI_DID: Did = DidKey.create(pfiKeyManager)

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

  fun getOffering(requiredClaims: List<PresentationDefinitionV2> = listOf(getPresentationDefinition())) =
    Offering.create(
      from = PFI_DID.uri,
      OfferingData(
        description = "my fake offering",
        payoutUnitsPerPayinUnit = "1",
        payinCurrency = CurrencyDetails("AUD"),
        payoutCurrency = CurrencyDetails("USDC"),
        payinMethods = listOf(),
        payoutMethods = listOf(),
        requiredClaims = requiredClaims
      )
    )

  fun getRfq(offeringId: TypeID = TypeID(ResourceKind.offering.name), claims: List<String> = emptyList()) = Rfq.create(
    PFI_DID.uri,
    ALICE_DID.uri,
    RfqData(
      offeringId = offeringId,
      payinSubunits = "1000",
      payinMethod = SelectedPaymentMethod("BTC_ADDRESS", mapOf("address" to 123456)),
      payoutMethod = SelectedPaymentMethod("MOMO", mapOf("phone_number" to 123456)),
      claims = claims
    )
  )

  fun getQuote() = Quote.create(
    ALICE_DID.uri, PFI_DID.uri, TypeID(MessageKind.rfq.name),
    QuoteData(
      expiresAt = OffsetDateTime.now().plusDays(1),
      payin = QuoteDetails("AUD", "1000", "0"),
      payout = QuoteDetails("BTC", "12", "0")
    )
  )

  fun getClose() = Close.create(ALICE_DID.uri, PFI_DID.uri, TypeID(MessageKind.rfq.name), CloseData("test reason"))

  fun getOrder() = Order.create(PFI_DID.uri, ALICE_DID.uri, TypeID(MessageKind.rfq.name))

  fun getOrderStatus() = OrderStatus.create(
    ALICE_DID.uri, PFI_DID.uri, TypeID(MessageKind.rfq.name), OrderStatusData("PENDING")
  )

  fun getOrderStatusWithInvalidDid(): OrderStatus {
    val os = OrderStatus.create(
      "alice", "pfi", TypeID(MessageKind.rfq.name), OrderStatusData("PENDING")
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
