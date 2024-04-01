package tbdex.sdk.protocol

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.support.appendName
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import org.junit.jupiter.api.assertDoesNotThrow
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ValidatorTest {

  @Test
  fun validateSucceedsWithProperRfqMessage() {
    val rfq = TestData.getRfq()
    rfq.sign(TestData.ALICE_DID)
    assertDoesNotThrow {
      val jsonRfq = Json.jsonMapper.convertValue<JsonNode>(rfq)
      Validator.validate(jsonRfq, "message")
      Validator.validate(jsonRfq.get("data"), rfq.metadata.kind.name)
    }
  }

  @Test
  fun validateFailsWithProperOrderMessageAndInvalidSchemaName() {
    val order = TestData.getOrder()
    order.sign(TestData.ALICE_DID)

    val jsonOrder = Json.jsonMapper.convertValue<JsonNode>(order)
    val exception = assertFailsWith<ValidatorException> {
      Validator.validate(jsonOrder, "asdf")
    }

    exception.message?.let { assertContains(it, "No schema with name") }
  }

  @Test
  fun validateFailsWithProperOrderStatusMessageAndWrongSchemaName() {
    val orderStatus = TestData.getOrderStatus()
    orderStatus.sign(TestData.ALICE_DID)

    val jsonOrderStatus = Json.jsonMapper.convertValue<JsonNode>(orderStatus)
    val exception = assertFailsWith<ValidatorException> {
      Validator.validate(jsonOrderStatus, "quote")
    }
    exception.message?.let { assertContains(it, "invalid payload") }
  }

  @Test
  fun validateFailsWithImproperOrderStatusMessage() {
    val orderStatus = TestData.getOrderStatusWithInvalidDid()
    orderStatus.sign(TestData.ALICE_DID)

    val jsonOrderStatus = Json.jsonMapper.convertValue<JsonNode>(orderStatus)

    val exception = assertFailsWith<ValidatorException> {
      Validator.validate(jsonOrderStatus, "message")
      Validator.validate(jsonOrderStatus.get("data"), orderStatus.metadata.kind.name)
    }

    assertEquals(2, exception.errors.size)

      assertThat(exception.errors[0]).appendName("$.metadata.from: does not match")
      assertThat(exception.errors[1]).appendName("$.metadata.to: does not match")
  }

  @Test
  fun validateFailsWithInvalidRfqData() {
    val stringRfqWithoutPayinAmount = """
      {
        "metadata": {
          "from": "did:dht:zonxnmszug57yehahmwaxnxpasj4pbp3odzh5xckzm4tmhyxzr7y",
          "to": "did:dht:bbex7sdsb55sdh9ptqoixmcdu3b719y3ujhfcq3eqirb4eftcdyo",
          "protocol": "1.0",
          "kind": "rfq",
          "id": "rfq_01htbm8qbfe1x9vvqr58xmbzzp",
          "exchangeId": "rfq_01htbm8qbfe1x9vvqr58xmbzzp",
          "createdAt": "2024-04-01T01:27:03.791Z"
        },
        "data": {
          "offeringId": "offering_01htbm8qbeer8vve26b7tfb00e",
          "payin": {
            "kind": "DEBIT_CARD",
            "paymentDetailsHash": "paXb7KC1Dsa1pOo1LzC3fVlGg96EQ8AyZ_tKS8WqECM"
          },
          "payout": {
            "kind": "BTC_ADDRESS",
            "paymentDetailsHash": "bHZ0RBDHEY02qWR6FGLiCzztrDF6nPsHmnOzUPmosBY"
          },
          "claimsHash": "QKEZisko621evX72O0oi9I1C8bFivl9zPW8Qe9frgk8"
        },
        "privateData": {
          "salt": "I85AVujHLNZAAjedZwI7eA",
          "payin": {
            "paymentDetails": {
              "cardNumber": "1234567890123456",
              "expiryDate": "12/22",
              "cardHolderName": "Ephraim Bartholomew Winthrop",
              "cvv": "123"
            }
          },
          "payout": {
            "paymentDetails": {
              "btcAddress": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"
            }
          },
          "claims": [
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJFZERTQSIsImtpZCI6ImRpZDpkaHQ6em9ueG5tc3p1ZzU3eWVoYWhtd2F4bnhwYXNqNHBicDNvZHpoNXhja3ptNHRtaHl4enI3eSMwIn0.eyJ2YyI6eyJAY29udGV4dCI6WyJodHRwczovL3d3dy53My5vcmcvMjAxOC9jcmVkZW50aWFscy92MSJdLCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiUHV1cHV1Q3JlZGVudGlhbCJdLCJpZCI6InVybjp1dWlkOjA3M2Q0NmZmLWY0MGEtNGM0Ni04OTNmLWZhYmY4NDk2NDUzNSIsImlzc3VlciI6ImRpZDpkaHQ6em9ueG5tc3p1ZzU3eWVoYWhtd2F4bnhwYXNqNHBicDNvZHpoNXhja3ptNHRtaHl4enI3eSIsImlzc3VhbmNlRGF0ZSI6IjIwMjQtMDQtMDFUMDE6Mjc6MDNaIiwiY3JlZGVudGlhbFN1YmplY3QiOnsiaWQiOiJkaWQ6ZGh0OnpvbnhubXN6dWc1N3llaGFobXdheG54cGFzajRwYnAzb2R6aDV4Y2t6bTR0bWh5eHpyN3kiLCJiZWVwIjoiYm9vcCJ9fSwiaXNzIjoiZGlkOmRodDp6b254bm1zenVnNTd5ZWhhaG13YXhueHBhc2o0cGJwM29kemg1eGNrem00dG1oeXh6cjd5Iiwic3ViIjoiZGlkOmRodDp6b254bm1zenVnNTd5ZWhhaG13YXhueHBhc2o0cGJwM29kemg1eGNrem00dG1oeXh6cjd5In0.kdm7OWMrNuu2iWJHaANaNV8IZiMYRq0VpWOq2wEeOvJm1xgGNAUlhZrYu1UahfBa8060kzGoKLYYLbtaoIbxCw"
          ]
        },
        "signature": "blah"
      }
    """.trimIndent()

    val jsonRfq = Json.jsonMapper.readTree(stringRfqWithoutPayinAmount)
    val exception = assertFailsWith<ValidatorException> {
      Validator.validate(jsonRfq, "message")
      Validator.validate(jsonRfq.get("data"), "rfq")
    }

    assertEquals(1, exception.errors.size)
    assertThat(exception.errors).contains("$.payin.amount: is missing but it is required")
  }
}
