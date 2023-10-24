package tbdex.sdk.protocol.models

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.assertDoesNotThrow
import tbdex.sdk.protocol.TestData
import tbdex.sdk.protocol.serialization.Json
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.DidIonManager
import kotlin.test.Test
import kotlin.test.assertIs

class OfferingTest {
  @Test
  fun `can create a new offering`() {
    val offering = Offering.create(
      from = TestData.PFI,
      OfferingData(
        description = "my fake offering",
        payoutUnitsPerPayinUnit = "1",
        payinCurrency = CurrencyDetails("AUD"),
        payoutCurrency = CurrencyDetails("BTC"),
        payinMethods = listOf(),
        payoutMethods = listOf(),
        requiredClaims = TestData.getPresentationDefinition()
      )
    )

    assertAll {
      assertThat(offering.data.description).isEqualTo("my fake offering")
      assertThat(offering.metadata.id.prefix).isEqualTo("offering")
    }
  }

  @Test
  fun `can parse offering from a json string`() {
    val offering = TestData.getOffering()
    offering.sign(TestData.PFI_DID)
    val jsonResource = offering.toString()
    val parsed = Resource.parse(jsonResource)

    assertIs<Offering>(parsed)
    assertThat(parsed.toString()).isEqualTo(jsonResource)
  }

  @Test
  fun `can parse offering signed with an ION DID`() {
    val offering = """{
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
    }"""

    val parsed = Resource.parse(offering)
    assertIs<Offering>(parsed)
    assertThat(parsed.toString()).isEqualTo(offering)
  }

  @Test
  fun `can parse IOD DID offering from a json string`() {
    val did = DidIonManager.create(InMemoryKeyManager())
    val offering = TestData.getOffering()
    offering.sign(did)
    val jsonResource = offering.toString()
    val parsed = Resource.parse(jsonResource)

    assertIs<Offering>(parsed)
    assertThat(parsed.toString()).isEqualTo(jsonResource)
  }

  @Test
  fun `can parse an offering`() {
    val offering = TestData.getOffering()
    offering.sign(TestData.ALICE_DID)

    assertDoesNotThrow { Resource.parse(Json.stringify(offering)) }
  }
}
