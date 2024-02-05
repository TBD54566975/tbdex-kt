package tbdex.sdk.protocol

import assertk.assertThat
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
          "from": "did:key:z6MkpkvGVrxxTVbo56mvbSiF6iCKNev56wqoMcHHowqUqvKQ",
          "to": "did:ex:pfi",
          "kind": "rfq",
          "id": "rfq_01hkx53kgafbmrg2xp87n5htfb",
          "exchangeId": "rfq_01hkx53kgafbmrg2xp87n5htfb",
          "createdAt": "2024-01-11T20:58:34.378Z"
        },
        "data": {
          "offeringId": "abcd123",
          "payinMethod": {
            "kind": "DEBIT_CARD",
            "paymentDetails": {
              "cardNumber": "1234567890123456",
              "expiryDate": "12/22",
              "cardHolderName": "Ephraim Bartholomew Winthrop",
              "cvv": "123"
            }
          },
          "payoutMethod": {
            "kind": "BTC_ADDRESS",
            "paymentDetails": {
              "btcAddress": "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"
            }
          },
          "claims": [
            ""
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
    assertContains(exception.errors, "$.payinAmount: is missing but it is required")
  }
}
