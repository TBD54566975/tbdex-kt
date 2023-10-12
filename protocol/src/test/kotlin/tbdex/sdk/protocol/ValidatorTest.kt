package tbdex.sdk.protocol

import org.everit.json.schema.ValidationException
import org.json.JSONObject
import org.junit.jupiter.api.assertDoesNotThrow
import tbdex.sdk.protocol.Json
import tbdex.sdk.protocol.Validator
import tbdex.sdk.protocol.ValidatorException
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
      Validator.validate(JSONObject(Json.stringify(rfq)), "message")
      Validator.validate(JSONObject(Json.stringify(rfq.data)), "rfq")
    }
  }

  @Test
  fun validateFailsWithProperOrderMessageAndInvalidSchemaName() {
    val order = TestData.getOrder()
    order.sign(TestData.ALICE_DID)
    val exception = assertFailsWith<ValidatorException> {
      Validator.validate(JSONObject(Json.stringify(order)), "asdf")
    }

    exception.message?.let { assertContains(it, "No schema with name") }
  }

  @Test
  fun validateFailsWithProperOrderStatusMessageAndWrongSchemaName() {
    val orderStatus = TestData.getOrderStatus()
    orderStatus.sign(TestData.ALICE_DID)
    val exception = assertFailsWith<ValidatorException> {
      Validator.validate(JSONObject(Json.stringify(orderStatus.data)), "quote")
    }
    exception.message?.let { assertContains(it, "Validation failed") }
  }

  @Test
  fun validateFailsWithImproperOrderStatusMessage() {
    val orderStatus = TestData.getOrderStatusWithInvalidDid()
    orderStatus.sign(TestData.ALICE_DID)
    val exception = assertFailsWith<Exception> {
      Validator.validate(JSONObject(Json.stringify(orderStatus)), "message")
      Validator.validate(JSONObject(Json.stringify(orderStatus.data)), "orderstatus")
    }

    val expectedValidationErrors = setOf(
      "#/metadata/from: string [pfi] does not match pattern",
      "#/metadata/to: string [alice] does not match pattern"
    )

    val validationException = exception.cause as ValidationException
    assertEquals(expectedValidationErrors.size, validationException.allMessages.size)

    for (message in validationException.allMessages) {
      expectedValidationErrors.contains(message)
    }
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
    val rfq = JSONObject(stringRfqWithoutPayinSubunits)
    val dataJson = rfq.getJSONObject("data")

    val exception = assertFailsWith<Exception> {
      Validator.validate(rfq, "message")
      Validator.validate(dataJson, "rfq")
    }

    val validationException = exception.cause as ValidationException
    assertEquals(1, validationException.allMessages.size)
    assertEquals("#: required key [payinSubunits] not found", validationException.allMessages[0])

  }
}
