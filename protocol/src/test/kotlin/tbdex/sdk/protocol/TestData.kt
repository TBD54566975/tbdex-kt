package tbdex.sdk.protocol

import com.danubetech.verifiablecredentials.CredentialSubject
import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.CloseData
import tbdex.sdk.protocol.models.CurrencyDetails
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.OfferingData
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.OrderStatus
import tbdex.sdk.protocol.models.OrderStatusData
import tbdex.sdk.protocol.models.PaymentInstruction
import tbdex.sdk.protocol.models.PaymentInstructions
import tbdex.sdk.protocol.models.PaymentMethod
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.QuoteData
import tbdex.sdk.protocol.models.QuoteDetails
import tbdex.sdk.protocol.models.ResourceKind
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.models.RfqData
import tbdex.sdk.protocol.models.SelectedPaymentMethod
import tbdex.sdk.protocol.serialization.Json
import web5.sdk.credentials.ConstraintsV2
import web5.sdk.credentials.FieldV2
import web5.sdk.credentials.InputDescriptorV2
import web5.sdk.credentials.PresentationDefinitionV2
import web5.sdk.credentials.VcDataModel
import web5.sdk.credentials.VerifiableCredential
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

  fun getOffering(from: String = PFI_DID.uri, requiredClaims: PresentationDefinitionV2 = getPresentationDefinition()) =
    Offering.create(
      from = from,
      OfferingData(
        description = "A sample offering",
        payoutUnitsPerPayinUnit = "1",
        payinCurrency = CurrencyDetails("AUD", "1", "10000"),
        payoutCurrency = CurrencyDetails("USDC"),
        payinMethods = listOf(
          PaymentMethod(
            kind = "BTC_ADDRESS",
            requiredPaymentDetails = requiredPaymentDetailsSchema()
          )
        ),
        payoutMethods = listOf(
          PaymentMethod(
            kind = "MOMO",
            requiredPaymentDetails = requiredPaymentDetailsSchema()
          )
        ),
        requiredClaims = requiredClaims
      )
    )

  fun getRfq(
    offeringId: TypeId = TypeId.generate(ResourceKind.offering.name),
    claims: List<String> = emptyList()
  ) = Rfq.create(
    to = PFI_DID.uri,
    from = ALICE_DID.uri,
    rfqData = RfqData(
      offeringId = offeringId,
      payinSubunits = "1000",
      payinMethod = SelectedPaymentMethod("BTC_ADDRESS", mapOf("address" to "123456")),
      payoutMethod = SelectedPaymentMethod(
        "MOMO", mapOf(
        "phoneNumber" to "+254712345678",
        "accountHolderName" to "Alfred Holder"
      )
      ),
      claims = claims
    )
  )

  fun getQuote() = Quote.create(
    ALICE_DID.uri, PFI_DID.uri, TypeId.generate(MessageKind.rfq.name),
    QuoteData(
      expiresAt = OffsetDateTime.now().plusDays(1),
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

  fun getClose() = Close.create(
    to = ALICE_DID.uri,
    from = PFI_DID.uri,
    exchangeId = TypeId.generate(MessageKind.rfq.name),
    closeData = CloseData("test reason")
  )

  fun getOrder() = Order.create(
    to = PFI_DID.uri,
    from = ALICE_DID.uri,
    exchangeId = TypeId.generate(MessageKind.rfq.name)
  )

  fun getOrderStatus() = OrderStatus.create(
    to = ALICE_DID.uri,
    from = PFI_DID.uri,
    exchangeId = TypeId.generate(MessageKind.rfq.name),
    orderStatusData = OrderStatusData("PENDING")
  )

  fun getOrderStatusWithInvalidDid(): OrderStatus {
    val os = OrderStatus.create(
      "alice", "pfi", TypeId.generate(MessageKind.rfq.name), OrderStatusData("PENDING")
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

  private fun requiredPaymentDetailsSchema() = Json.jsonMapper.readTree(
    """
    {
      "${'$'}schema": "http://json-schema.org/draft-07/schema",
      "additionalProperties": false,
      "type": "object",
      "properties": {
        "phoneNumber": {
          "minLength": 12,
          "pattern": "^+2547[0-9]{8}${'$'}",
          "description": "Mobile Money account number of the Recipient",
          "type": "string",
          "title": "Phone Number",
          "maxLength": 12
        },
        "accountHolderName": {
          "pattern": "^[A-Za-zs'-]+${'$'}",
          "description": "Name of the account holder as it appears on the Mobile Money account",
          "type": "string",
          "title": "Account Holder Name",
          "maxLength": 32
        }
      },
      "required": [
        "accountNumber",
        "accountHolderName"
      ]
    }
  """.trimIndent()
  )
}
