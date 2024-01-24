package tbdex.sdk.httpclient

import com.danubetech.verifiablecredentials.CredentialSubject
import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.models.CurrencyDetails
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.OfferingData
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.QuoteData
import tbdex.sdk.protocol.models.QuoteDetails
import tbdex.sdk.protocol.models.ResourceKind
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.models.RfqData
import tbdex.sdk.protocol.models.SelectedPaymentMethod
import web5.sdk.credentials.VcDataModel
import web5.sdk.credentials.VerifiableCredential
import web5.sdk.credentials.model.ConstraintsV2
import web5.sdk.credentials.model.FieldV2
import web5.sdk.credentials.model.InputDescriptorV2
import web5.sdk.credentials.model.PresentationDefinitionV2
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.Did
import web5.sdk.dids.methods.key.DidKey
import java.net.URI
import java.time.OffsetDateTime
import java.util.Date
import java.util.UUID

object TestData {
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

  fun getOffering(requiredClaims: PresentationDefinitionV2 = getPresentationDefinition()): Offering {
    val offering = Offering.create(
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
    offering.sign(PFI_DID)
    return offering
  }
  fun getRfq(
    to: String = PFI_DID.uri,
    offeringId: TypeId = TypeId.generate(ResourceKind.offering.name),
    claims: List<String> = emptyList()
  ): Rfq {
    val rfq = Rfq.create(
      to = to,
      from = ALICE_DID.uri,
      rfqData = RfqData(
        offeringId = offeringId,
        payinAmount = "10.00",
        payinMethod = SelectedPaymentMethod("BTC_ADDRESS", mapOf("address" to 123456)),
        payoutMethod = SelectedPaymentMethod("MOMO", mapOf("phone_number" to 123456)),
        claims = claims
      )
    )
    rfq.sign(ALICE_DID)
    return rfq
  }
  fun getQuote(): Quote {
    val quote = Quote.create(
      ALICE_DID.uri, PFI_DID.uri, TypeId.generate(MessageKind.rfq.name),
      QuoteData(
        expiresAt = OffsetDateTime.now().plusDays(1),
        payin = QuoteDetails("AUD", "10.00", "0.0"),
        payout = QuoteDetails("BTC", "0.12", "0.0")
      )
    )
    quote.sign(PFI_DID)
    return quote
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
