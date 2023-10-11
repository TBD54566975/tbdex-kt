package protocol

import Validator
import org.json.JSONObject
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class ValidatorTest {

  @Test
  fun validateSucceedsWithProperRfqMessage() {
    val rfq = TestData.getRfq()
    rfq.sign("fakepk", "fakekid")
    assertDoesNotThrow {
      Validator.validate(JSONObject(Json.stringify(rfq)), "message")
      Validator.validate(JSONObject(Json.stringify(rfq.data)), "rfq")
    }
  }

  @Test
  fun validateFailsWithProperOrderMessageAndWrongSchemaName() {
    val order = TestData.getOrder()
    order.sign("fakepk", "fakekid")
    val exception = assertFailsWith<Exception> {
      Validator.validate(JSONObject(Json.stringify(order)), "resource")
    }
    exception.message?.let { assertContains(it, "Validation failed") }
  }

  @Test
  fun validateFailsWithProperOrderStatusMessageAndWrongSchemaName() {
    val orderStatus = TestData.getOrderStatus()
    orderStatus.sign("fakepk", "fakekid")
    val exception = assertFailsWith<Exception> {
      Validator.validate(JSONObject(Json.stringify(orderStatus.data)), "quote")
    }
    exception.message?.let { assertContains(it, "Validation failed") }
  }

  @Test
  fun validateFailsWithImproperOrderStatusMessage() {
    val orderStatus = TestData.getOrderStatusWithInvalidDid()
    orderStatus.sign("fakepk", "fakekid")
    val exception = assertFailsWith<Exception> {
      Validator.validate(JSONObject(Json.stringify(orderStatus)), "message")
      Validator.validate(JSONObject(Json.stringify(orderStatus.data)), "orderstatus")
    }
    exception.message?.let {
      assertContains(it, "#/metadata/from: string [fakeDid] does not match pattern")
      assertContains(it, "#/metadata/to: string [fakeDid] does not match pattern")
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
    println(exception.message)
    exception.message?.let {
      assertEquals(it, "Validation failed, errors: [#: required key [payinSubunits] not found]")
    }

  }
}