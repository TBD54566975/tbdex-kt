package protocol

import com.danubetech.verifiablecredentials.CredentialSubject
import models.Close
import models.CloseData
import models.CurrencyDetails
import models.MessageKind
import models.Offering
import models.OfferingData
import models.Order
import models.OrderStatus
import models.OrderStatusData
import models.Quote
import models.QuoteData
import models.QuoteDetails
import models.ResourceKind
import models.Rfq
import models.RfqData
import models.SelectedPaymentMethod
import typeid.TypeID
import web5.credentials.ConstraintsV2
import web5.credentials.FieldV2
import web5.credentials.InputDescriptorV2
import web5.credentials.PresentationDefinitionV2
import web5.credentials.VcDataModel
import web5.credentials.VerifiableCredential
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.Did
import web5.sdk.dids.DidKey
import java.net.URI
import java.time.OffsetDateTime
import java.util.Date
import java.util.UUID

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

  private fun buildField(id: String? = null, vararg paths: String): FieldV2 {
    return FieldV2(id = id, path = paths.toList())
  }

  fun getOffering(requiredClaims: List<PresentationDefinitionV2> = listOf(getPresentationDefinition())) =
    Offering.create(
      from = PFI,
      OfferingData(
        description = "my fake offering",
        payoutUnitsPerPayinUnit = 1,
        payinCurrency = CurrencyDetails("AUD"),
        payoutCurrency = CurrencyDetails("USDC"),
        payinMethods = listOf(),
        payoutMethods = listOf(),
        requiredClaims = requiredClaims
      )
    )

  fun getRfq(offeringId: TypeID = TypeID(ResourceKind.offering.name), claims: List<String> = emptyList()) = Rfq.create(
    PFI,
    ALICE,
    RfqData(
      offeringID = offeringId,
      payinSubunits = 10_00,
      payinMethod = SelectedPaymentMethod("BTC_ADDRESS", mapOf("address" to 123456)),
      payoutMethod = SelectedPaymentMethod("MOMO", mapOf("phone_number" to 123456)),
      claims = claims
    )
  )

  fun getQuote() = Quote.create(
    ALICE, PFI, TypeID(MessageKind.rfq.name),
    QuoteData(
      expiresAt = OffsetDateTime.now().plusDays(1),
      payin = QuoteDetails("AUD", 10_00, 0),
      payout = QuoteDetails("BTC", 12, 0)
    )
  )

  fun getClose() = Close.create(ALICE, PFI, TypeID(MessageKind.rfq.name), CloseData("test reason"))

  fun getOrder() = Order.create(PFI, ALICE, TypeID(MessageKind.rfq.name))

  fun getOrderStatus() = OrderStatus.create(
    ALICE, PFI, TypeID(MessageKind.rfq.name), OrderStatusData("test status")
  )

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
