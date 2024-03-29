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
          "from": "did:dht:9kkuh34q7nkd4tphbcg7py9h1g16iftbtskesi9courdwj96q3sy",
          "to": "did:dht:8rqqxczxhdugndj5mykiahy7y4zg4zkk3jajr6qyo5owdrgqqx3y",
          "protocol": "1.0",
          "kind": "rfq",
          "id": "rfq_01ht6fh3fsf9z8ekh5tyjbsmwk",
          "exchangeId": "rfq_01ht6fh3fsf9z8ekh5tyjbsmwk",
          "createdAt": "2024-03-30T01:28:03.321Z"
        },
        "data": {
          "offeringId": "offering_01ht6fh3fre7t8zfc6j97t2pe1",
          "payin": {
            "kind": "DEBIT_CARD",
            "paymentDetailsHash": "k8nU7MKYNb140u3pN8FO5ndAK3WDJ8lQDEsYA0H9fWA"
          },
          "payout": {
            "kind": "BTC_ADDRESS",
            "paymentDetailsHash": "xwyJqXOcLsYBBojNlVHZGZzlhZvswFPUh3xsLxus_EU"
          },
          "claimsHash": "o16anc251kJb3uyYKzPdlS5SdhGomylHZ8_YZ7CBbgU"
        },
        "privateData": {
          "salt": "�>�����\u0014�]�NɈ",
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
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJFZERTQSIsImtpZCI6ImRpZDpkaHQ6OWtrdWgzNHE3bmtkNHRwaGJjZzdweTloMWcxNmlmdGJ0c2tlc2k5Y291cmR3ajk2cTNzeSMwIn0.eyJ2YyI6eyJAY29udGV4dCI6WyJodHRwczovL3d3dy53My5vcmcvMjAxOC9jcmVkZW50aWFscy92MSJdLCJ0eXBlIjpbIlZlcmlmaWFibGVDcmVkZW50aWFsIiwiUHV1cHV1Q3JlZGVudGlhbCJdLCJpZCI6InVybjp1dWlkOmY5YWVlNjdjLTQ1NmUtNGU1Yy05NDg3LWM3MjM2ZmQyMWUxNiIsImlzc3VlciI6ImRpZDpkaHQ6OWtrdWgzNHE3bmtkNHRwaGJjZzdweTloMWcxNmlmdGJ0c2tlc2k5Y291cmR3ajk2cTNzeSIsImlzc3VhbmNlRGF0ZSI6IjIwMjQtMDMtMzBUMDE6Mjg6MDNaIiwiY3JlZGVudGlhbFN1YmplY3QiOnsiaWQiOiJkaWQ6ZGh0Ojlra3VoMzRxN25rZDR0cGhiY2c3cHk5aDFnMTZpZnRidHNrZXNpOWNvdXJkd2o5NnEzc3kiLCJiZWVwIjoiYm9vcCJ9fSwiaXNzIjoiZGlkOmRodDo5a2t1aDM0cTdua2Q0dHBoYmNnN3B5OWgxZzE2aWZ0YnRza2VzaTljb3VyZHdqOTZxM3N5Iiwic3ViIjoiZGlkOmRodDo5a2t1aDM0cTdua2Q0dHBoYmNnN3B5OWgxZzE2aWZ0YnRza2VzaTljb3VyZHdqOTZxM3N5In0.kxFwk-eCpCrO1m7lYAlqEioGjVZ1vlM-DctE52atVKa6Egn-DS0nqmqDcXo_yRmAUylU5E-7lOPsP7N90DbAAA"
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
