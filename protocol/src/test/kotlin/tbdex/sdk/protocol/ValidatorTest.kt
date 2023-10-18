package tbdex.sdk.protocol

import assertk.assertThat
import assertk.assertions.support.appendName
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.convertValue
import org.junit.jupiter.api.assertDoesNotThrow
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
    val stringRfqWithoutPayinSubunits = """
      {
        "metadata": {
          "kind": "rfq",
          "to": "did:ion:EiBFU3435y86IIthWg9OSMCpx-cjtBV0RTGHGPjs6TxQag:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJkd24tc2lnIiwicHVibGljS2V5SndrIjp7ImNydiI6IkVkMjU1MTkiLCJrdHkiOiJPS1AiLCJ4IjoiOU5xN3BObG0xV1BFa2lwcDRpSXNsYTc5RVctNnc5b1NLWWhVZWVuX3lwcyJ9LCJwdXJwb3NlcyI6WyJhdXRoZW50aWNhdGlvbiJdLCJ0eXBlIjoiSnNvbldlYktleTIwMjAifV0sInNlcnZpY2VzIjpbeyJpZCI6InBmaSIsInNlcnZpY2VFbmRwb2ludCI6Imh0dHBzOi8vcGZpLnllbGxvd2NhcmQuZW5naW5lZXJpbmciLCJ0eXBlIjoiUEZJIn1dfX1dLCJ1cGRhdGVDb21taXRtZW50IjoiRWlBQ2cxRWFIaXFjZUQ1N1VBcXZ3bF9uaHplWjZ6MTBacVF0UWV2d0xDelB5dyJ9LCJzdWZmaXhEYXRhIjp7ImRlbHRhSGFzaCI6IkVpQ0tYTDZDRS1hZlNfUUdKbmxNaHdPV0dvNDR0VEtHZTlZQ041QjN1bzZ1M3ciLCJyZWNvdmVyeUNvbW1pdG1lbnQiOiJFaUNJSkFBME11a21Pb1Uxc3JLSjdiOTNDZGVJSy0zMk5GVnl6ZVRzektzYzNRIn19",
          "from": "did:ion:EiBFU3435y86IIthWg9OSMCpx-cjtBV0RTGHGPjs6TxQag:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJkd24tc2lnIiwicHVibGljS2V5SndrIjp7ImNydiI6IkVkMjU1MTkiLCJrdHkiOiJPS1AiLCJ4IjoiOU5xN3BObG0xV1BFa2lwcDRpSXNsYTc5RVctNnc5b1NLWWhVZWVuX3lwcyJ9LCJwdXJwb3NlcyI6WyJhdXRoZW50aWNhdGlvbiJdLCJ0eXBlIjoiSnNvbldlYktleTIwMjAifV0sInNlcnZpY2VzIjpbeyJpZCI6InBmaSIsInNlcnZpY2VFbmRwb2ludCI6Imh0dHBzOi8vcGZpLnllbGxvd2NhcmQuZW5naW5lZXJpbmciLCJ0eXBlIjoiUEZJIn1dfX1dLCJ1cGRhdGVDb21taXRtZW50IjoiRWlBQ2cxRWFIaXFjZUQ1N1VBcXZ3bF9uaHplWjZ6MTBacVF0UWV2d0xDelB5dyJ9LCJzdWZmaXhEYXRhIjp7ImRlbHRhSGFzaCI6IkVpQ0tYTDZDRS1hZlNfUUdKbmxNaHdPV0dvNDR0VEtHZTlZQ041QjN1bzZ1M3ciLCJyZWNvdmVyeUNvbW1pdG1lbnQiOiJFaUNJSkFBME11a21Pb1Uxc3JLSjdiOTNDZGVJSy0zMk5GVnl6ZVRzektzYzNRIn19",
          "id": "rfq_7zzzzzzypzfwx8006f7800086j",
          "exchangeId": "rfq_7zzzzzzypzfwx8006f7800086j",
          "createdAt": "2023-10-11T22:22:00.904Z"
        },
        "data": {
          "offeringId": "offering_01hcgdawywedyr004kfm001sme",
          "payinMethod": {
            "kind": "BTC_ADDRESS",
            "paymentDetails": {
              "address": 123456
            }
          },
          "payoutMethod": {
            "kind": "MOMO",
            "paymentDetails": {
              "phone_number": 123456
            }
          },
          "claims": []
        },
        "signature": "blah"
      }
    """.trimIndent()

    val jsonRfq = Json.jsonMapper.readTree(stringRfqWithoutPayinSubunits)
    val exception = assertFailsWith<ValidatorException> {
      Validator.validate(jsonRfq, "message")
      Validator.validate(jsonRfq.get("data"), "rfq")
    }

    assertEquals(1, exception.errors.size)
    assertContains(exception.errors, "$.payinSubunits: is missing but it is required")
  }
}
