package tbdex.sdk.protocol

import com.danubetech.verifiablecredentials.CredentialSubject
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
import typeid.TypeID
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
  val ION_SIGNED_OFFERING_STRING = """{
      "metadata": {
        "from": "did:ion:EiDCYKaMtz8hWnylrPKaDsOqNoM973GWqfGCUIeLQesWcg:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJkd24tc2lnIiwicHVibGljS2V5SndrIjp7ImNydiI6IkVkMjU1MTkiLCJrdHkiOiJPS1AiLCJ4IjoidGdXWUF3ajlSRkhXaEJON2Fya0pnQTJKSUlDbHg2Zm54cjVjeE9jNm95SSJ9LCJwdXJwb3NlcyI6WyJhdXRoZW50aWNhdGlvbiJdLCJ0eXBlIjoiSnNvbldlYktleTIwMjAifV0sInNlcnZpY2VzIjpbeyJpZCI6InBmaSIsInNlcnZpY2VFbmRwb2ludCI6Imh0dHBzOi8vcGZpLnllbGxvd2NhcmQuZW5naW5lZXJpbmciLCJ0eXBlIjoiUEZJIn1dfX1dLCJ1cGRhdGVDb21taXRtZW50IjoiRWlCSk9ha3M4WmI2LXJueDdzMERnWnZqel9YS3NfUEJoN3BTcUgycUQzMXphQSJ9LCJzdWZmaXhEYXRhIjp7ImRlbHRhSGFzaCI6IkVpQWQxRTRSWVBEdlUtTUNlZnY3cUZUOEszaTVZcjNrZ3BnOWhiSkhsWXg0ZnciLCJyZWNvdmVyeUNvbW1pdG1lbnQiOiJFaUNXYzVzekFiWUpsMzVWci1Sdzl6ZE1hWDNlaGZPQUlBUHhEVnhsY3NjWWZBIn19",
        "kind": "offering",
        "id": "offering_01hd6p8q87ff3vcpx9bx3jj51z",
        "createdAt": "2023-10-20T14:01:44.967Z"
      },
      "data": {
        "description": "Selling NGN for BTC",
        "payoutCurrency": {
          "currencyCode": "NGN",
          "minSubunits": "100",
          "maxSubunits": "1000"
        },
        "payinCurrency": {
          "currencyCode": "BTC"
        },
        "payoutUnitsPerPayinUnit": "17572117.04",
        "payoutMethods": [
          {
            "kind": "BANK_Access Bank",
            "requiredPaymentDetails": {
              "{$}schema": "http://json-schema.org/draft-07/schema",
              "type": "object",
              "properties": {
                "accountNumber": {
                  "type": "string",
                  "description": "Bank account number of the recipient",
                  "title": "Bank Account Number",
                  "minLength": 10,
                  "maxLength": 10,
                  "pattern": "^[0-9]{8}${'$'}"
                },
                "reason": {
                  "title": "Reason for sending",
                  "description": "To abide by the travel rules and financial reporting requirements, the reason for sending money",
                  "type": "string"
                },
                "accountHolderName": {
                  "type": "string",
                  "title": "Account Holder Name",
                  "description": "Name of the account holder as it appears on the Bank Account",
                  "pattern": "^[A-Za-zs'-]+${'$'}",
                  "maxLength": 32
                }
              },
              "required": [
                "accountNumber",
                "accountHolderName",
                "reason"
              ],
              "additionalProperties": false
            },
            "feeSubunits": "100"
          },
          {
            "kind": "BANK_GT Bank",
            "requiredPaymentDetails": {
              "{$}schema": "http://json-schema.org/draft-07/schema",
              "type": "object",
              "properties": {
                "accountNumber": {
                  "type": "string",
                  "description": "Bank account number of the recipient",
                  "title": "Bank Account Number",
                  "minLength": 10,
                  "maxLength": 10,
                  "pattern": "^[0-9]{8}${'$'}"
                },
                "reason": {
                  "title": "Reason for sending",
                  "description": "To abide by the travel rules and financial reporting requirements, the reason for sending money",
                  "type": "string"
                },
                "accountHolderName": {
                  "type": "string",
                  "title": "Account Holder Name",
                  "description": "Name of the account holder as it appears on the Bank Account",
                  "pattern": "^[A-Za-zs'-]+${'$'}",
                  "maxLength": 32
                }
              },
              "required": [
                "accountNumber",
                "accountHolderName",
                "reason"
              ],
              "additionalProperties": false
            },
            "feeSubunits": "100"
          },
          {
            "kind": "BANK_United Bank for Africa",
            "requiredPaymentDetails": {
              "{$}schema": "http://json-schema.org/draft-07/schema",
              "type": "object",
              "properties": {
                "accountNumber": {
                  "type": "string",
                  "description": "Bank account number of the recipient",
                  "title": "Bank Account Number",
                  "minLength": 10,
                  "maxLength": 10,
                  "pattern": "^[0-9]{8}${'$'}"
                },
                "reason": {
                  "title": "Reason for sending",
                  "description": "To abide by the travel rules and financial reporting requirements, the reason for sending money",
                  "type": "string"
                },
                "accountHolderName": {
                  "type": "string",
                  "title": "Account Holder Name",
                  "description": "Name of the account holder as it appears on the Bank Account",
                  "pattern": "^[A-Za-zs'-]+${'$'}",
                  "maxLength": 32
                }
              },
              "required": [
                "accountNumber",
                "accountHolderName",
                "reason"
              ],
              "additionalProperties": false
            },
            "feeSubunits": "100"
          }
        ],
        "payinMethods": [
          {
            "kind": "NGN_ADDRESS",
            "requiredPaymentDetails": {
              "{$}schema": "http://json-schema.org/draft-07/schema",
              "type": "object",
              "properties": {
                "walletAddress": {
                  "type": "string",
                  "description": "your NGN wallet address",
                  "title": "BTC Address"
                }
              },
              "required": [
                "walletAddress"
              ],
              "additionalProperties": false
            }
          }
        ],
        "requiredClaims": {
          "id": "70469e63-da7f-4284-86b4-f616670ccfac",
          "input_descriptors": [
            {
              "id": "8ffe0605-8bbb-4866-ac02-256c248d8a82",
              "constraints": {
                "fields": [
                  {
                    "path": [
                      "${'$'}.issuer"
                    ],
                    "filter": {
                      "type": "string",
                      "const": "did:ion:EiD6Jcwrqb5lFLFWyW59uLizo5lBuChieiqtd0TFN0xsng:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJ6cC1mNnFMTW1EazZCNDFqTFhIXy1kd0xOLW9DS2lTcDJaa19WQ2t4X3ZFIiwicHVibGljS2V5SndrIjp7ImNydiI6InNlY3AyNTZrMSIsImt0eSI6IkVDIiwieCI6IjNmVFk3VXpBaU9VNVpGZ05VVjl3bm5pdEtGQk51RkNPLWxlRXBDVzhHOHMiLCJ5IjoidjJoNlRqTDF0TnYwSDNWb09Fbll0UVBxRHZOVC0wbVdZUUdLTGRSakJ3ayJ9LCJwdXJwb3NlcyI6WyJhdXRoZW50aWNhdGlvbiJdLCJ0eXBlIjoiSnNvbldlYktleTIwMjAifV0sInNlcnZpY2VzIjpbXX19XSwidXBkYXRlQ29tbWl0bWVudCI6IkVpQjk3STI2bmUwdkhXYXduODk1Y1dnVlE0cFF5NmN1OUFlSzV2aW44X3JVeXcifSwic3VmZml4RGF0YSI6eyJkZWx0YUhhc2giOiJFaURqSmlEdm9RekstRl94V05VVzlzMTBUVmlpdEI0Z1JoS09iYlh2S1pwdlNRIiwicmVjb3ZlcnlDb21taXRtZW50IjoiRWlCbEk1NWx6b3JoeE42TVBqUlZtV2ZZY3MxNzNKOFk3S0hTeU5LcmZiTzVfdyJ9fQ"
                    }
                  },
                  {
                    "path": [
                      "${'$'}.type[*]"
                    ],
                    "filter": {
                      "type": "string",
                      "pattern": "^SanctionCredential${'$'}"
                    }
                  }
                ]
              }
            }
          ]
        }
      },
      "signature": "eyJhbGciOiJFZERTQSIsImtpZCI6ImRpZDppb246RWlEQ1lLYU10ejhoV255bHJQS2FEc09xTm9NOTczR1dxZkdDVUllTFFlc1djZzpleUprWld4MFlTSTZleUp3WVhSamFHVnpJanBiZXlKaFkzUnBiMjRpT2lKeVpYQnNZV05sSWl3aVpHOWpkVzFsYm5RaU9uc2ljSFZpYkdsalMyVjVjeUk2VzNzaWFXUWlPaUprZDI0dGMybG5JaXdpY0hWaWJHbGpTMlY1U25kcklqcDdJbU55ZGlJNklrVmtNalUxTVRraUxDSnJkSGtpT2lKUFMxQWlMQ0o0SWpvaWRHZFhXVUYzYWpsU1JraFhhRUpPTjJGeWEwcG5RVEpLU1VsRGJIZzJabTU0Y2pWamVFOWpObTk1U1NKOUxDSndkWEp3YjNObGN5STZXeUpoZFhSb1pXNTBhV05oZEdsdmJpSmRMQ0owZVhCbElqb2lTbk52YmxkbFlrdGxlVEl3TWpBaWZWMHNJbk5sY25acFkyVnpJanBiZXlKcFpDSTZJbkJtYVNJc0luTmxjblpwWTJWRmJtUndiMmx1ZENJNkltaDBkSEJ6T2k4dmNHWnBMbmxsYkd4dmQyTmhjbVF1Wlc1bmFXNWxaWEpwYm1jaUxDSjBlWEJsSWpvaVVFWkpJbjFkZlgxZExDSjFjR1JoZEdWRGIyMXRhWFJ0Wlc1MElqb2lSV2xDU2s5aGEzTTRXbUkyTFhKdWVEZHpNRVJuV25acWVsOVlTM05mVUVKb04zQlRjVWd5Y1VRek1YcGhRU0o5TENKemRXWm1hWGhFWVhSaElqcDdJbVJsYkhSaFNHRnphQ0k2SWtWcFFXUXhSVFJTV1ZCRWRsVXRUVU5sWm5ZM2NVWlVPRXN6YVRWWmNqTnJaM0JuT1doaVNraHNXWGcwWm5jaUxDSnlaV052ZG1WeWVVTnZiVzFwZEcxbGJuUWlPaUpGYVVOWFl6Vnpla0ZpV1Vwc016VldjaTFTZHpsNlpFMWhXRE5sYUdaUFFVbEJVSGhFVm5oc1kzTmpXV1pCSW4xOSNkd24tc2lnIn0..1W9tiiDggjXVjKg8YOvv7K44AVo6Rxn7KvuAel2v_ohbMglPAJhd1WZsIexx-i71UBoMHcKcFSRVG2Q8r__TBw"
    }""".trimIndent()

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

  fun getOffering(requiredClaims: PresentationDefinitionV2 = getPresentationDefinition()) =
    Offering.create(
      from = PFI_DID.uri,
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

  fun getRfq(offeringId: TypeID = TypeID(ResourceKind.offering.name), claims: List<String> = emptyList()) = Rfq.create(
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
    ALICE_DID.uri, PFI_DID.uri, TypeID(MessageKind.rfq.name),
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
    exchangeId = TypeID(MessageKind.rfq.name),
    closeData = CloseData("test reason")
  )

  fun getOrder() = Order.create(
    to = PFI_DID.uri,
    from = ALICE_DID.uri,
    exchangeId = TypeID(MessageKind.rfq.name)
  )

  fun getOrderStatus() = OrderStatus.create(
    to = ALICE_DID.uri,
    from = PFI_DID.uri,
    exchangeId = TypeID(MessageKind.rfq.name),
    orderStatusData = OrderStatusData("PENDING")
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
