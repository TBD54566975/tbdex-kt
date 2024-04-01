package tbdex.sdk.protocol.models

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.startsWith
import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import de.fxlae.typeid.TypeId
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import tbdex.sdk.protocol.TestData
import tbdex.sdk.protocol.ValidatorException
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.Test
import kotlin.test.assertIs

class RfqTest {
  @Test
  fun `can create a new rfq`() {
    val rfq = Rfq.create(
      to = TestData.PFI,
      from = TestData.ALICE,
      unhashedRfqData = UnhashedRfqData(
        offeringId = TypeId.generate(ResourceKind.offering.name).toString(),
        payin = UnhashedSelectedPayinMethod("BTC_ADDRESS", mapOf("address" to 123456), amount = "10.00"),
        payout = UnhashedSelectedPayoutMethod("MOMO", mapOf("phone_number" to 123456)),
        claims = emptyList()
      ),
      externalId = "P_12345"
    )

    assertAll {
      assertThat(rfq.metadata.id).startsWith("rfq")
      assertThat(rfq.metadata.protocol).isEqualTo("1.0")
      assertThat(rfq.data.payin.amount).isEqualTo("10.00")
      assertThat(rfq.metadata.externalId).isEqualTo("P_12345")
    }
  }

  @Test
  fun `can parse an rfq from a json string`() {
    val rfq = TestData.getRfq()
    rfq.sign(TestData.ALICE_DID)
    val jsonMessage = rfq.toString()
    val parsedMessage = Rfq.parse(jsonMessage)

    assertIs<Rfq>(parsedMessage)
    assertThat(parsedMessage.toString()).isEqualTo(jsonMessage)
  }

  @Test
  fun `parse() throws if json string is not an Rfq`() {
    val quote = TestData.getQuote()
    quote.sign(TestData.ALICE_DID)
    val jsonMessage = quote.toString()
    assertThrows<IllegalArgumentException> { Rfq.parse(jsonMessage) }
  }

  @Test
  fun `can validate a rfq`() {
    val rfq = TestData.getRfq()
    rfq.sign(TestData.ALICE_DID)

    assertDoesNotThrow { Rfq.parse(Json.stringify(rfq)) }
  }

  @Test
  @Disabled
  fun `verifyOfferingRequirements succeeds when claims satisfy pd`() {
    val offering = TestData.getOffering()
    val rfq = TestData.getRfq(offering.metadata.id, listOf(TestData.getVC().toString()))

    assertDoesNotThrow { rfq.verifyOfferingRequirements(offering) }
  }

  @Test
  @Disabled
  fun `verifyOfferingRequirements throws when claims do not satisfy pd`() {
    val offering = TestData.getOffering()
    val rfq = TestData.getRfq()

    assertThrows<IllegalArgumentException> { rfq.verifyOfferingRequirements(offering) }
  }

  @Test
  @Disabled
  fun `verifyOfferingRequirements throws when claims fail verification`() {
    val offering = TestData.getOffering()
    val rfq = TestData.getRfq()

    // distinguish that this is a verification failure
    assertThrows<IllegalArgumentException> { rfq.verifyOfferingRequirements(offering) }
  }

  @Nested
  inner class ParseRfqRequireAllPrivateData {

    @Test
    fun `throws if private data is missing but hashed fields are present in data`() {
      val rfq = TestData.getRfq()
      rfq.sign(TestData.ALICE_DID)

      var rfqJsonString = rfq.toString()
      val rfqJsonNode = Json.jsonMapper.readTree(rfqJsonString) as ObjectNode
      removeNestedJsonNodeProp(rfqJsonNode, "/privateData")
      rfqJsonString = rfqJsonNode.toString()

      assertThrows<IllegalArgumentException> { Rfq.parse(rfqJsonString, requireAllPrivateData = true) }
    }

    @Test
    fun `throws if salt is missing but hashed fields are present in data`() {
      val rfq = TestData.getRfq()
      rfq.sign(TestData.ALICE_DID)

      var rfqJsonString = rfq.toString()
      val rfqJsonNode = Json.jsonMapper.readTree(rfqJsonString) as ObjectNode
      removeNestedJsonNodeProp(rfqJsonNode, "/privateData/salt")
      rfqJsonString = rfqJsonNode.toString()

      assertThrows<IllegalArgumentException> { Rfq.parse(rfqJsonString, requireAllPrivateData = true) }
    }

    @Test
    fun `throws if privateData payin paymentDetails is missing but data payin paymentDetailsHash is present`() {
      val rfq = TestData.getRfq()
      rfq.sign(TestData.ALICE_DID)

      var rfqJsonString = rfq.toString()
      val rfqJsonNode = Json.jsonMapper.readTree(rfqJsonString) as ObjectNode
      removeNestedJsonNodeProp(rfqJsonNode, "/privateData/payin/paymentDetails")
      rfqJsonString = rfqJsonNode.toString()

      assertThrows<IllegalArgumentException> { Rfq.parse(rfqJsonString, requireAllPrivateData = true) }
    }

    @Test
    fun `throws if privateData payout paymentDetails is missing but data payout paymentDetailsHash is present`() {
      val rfq = TestData.getRfq()
      rfq.sign(TestData.ALICE_DID)

      var rfqJsonString = rfq.toString()
      val rfqJsonNode = Json.jsonMapper.readTree(rfqJsonString) as ObjectNode
      removeNestedJsonNodeProp(rfqJsonNode, "/privateData/payout/paymentDetails")
      rfqJsonString = rfqJsonNode.toString()

      assertThrows<IllegalArgumentException> { Rfq.parse(rfqJsonString, requireAllPrivateData = true) }
    }

    @Test
    fun `throws if privateData claims is missing but data claimsHash is present`() {
      val rfq = TestData.getRfq(claims = listOf(TestData.getVC().toString()))
      rfq.sign(TestData.ALICE_DID)

      var rfqJsonString = rfq.toString()
      val rfqJsonNode = Json.jsonMapper.readTree(rfqJsonString) as ObjectNode
      removeNestedJsonNodeProp(rfqJsonNode, "/privateData/claims")
      rfqJsonString = rfqJsonNode.toString()

      assertThrows<IllegalArgumentException> { Rfq.parse(rfqJsonString, requireAllPrivateData = true) }
    }

    @Test
    fun `throws if privateData payin paymentDetails is incorrect but data payin paymentDetailsHash is present`() {
      val rfq = TestData.getRfq()
      rfq.data.payin.paymentDetailsHash = "garbage"
      rfq.sign(TestData.ALICE_DID)

      val rfqJson = rfq.toString()

      assertThrows<IllegalArgumentException> { Rfq.parse(rfqJson, requireAllPrivateData = true) }
    }

    @Test
    fun `throws if privateData payout paymentDetails is incorrect but data payout paymentDetailsHash is present`() {
      val rfq = TestData.getRfq()
      rfq.data.payout.paymentDetailsHash = "garbage"
      rfq.sign(TestData.ALICE_DID)

      val rfqJson = rfq.toString()

      assertThrows<IllegalArgumentException> { Rfq.parse(rfqJson, requireAllPrivateData = true) }
    }

    @Test
    fun `throws if privateData claims is incorrect but data claimsHash is present`() {
      val rfq = TestData.getRfq(claims = listOf(TestData.getVC().toString()))
      rfq.data.claimsHash = "garbage"
      rfq.sign(TestData.ALICE_DID)

      val rfqJson = rfq.toString()

      assertThrows<IllegalArgumentException> { Rfq.parse(rfqJson, requireAllPrivateData = true) }
    }
  }

  @Nested
  inner class ParseRfqRequireAllPrivateDataFalse {

    @Test
    fun `throws if salt is missing but privateData is present`() {
      val rfq = TestData.getRfq()
      rfq.sign(TestData.ALICE_DID)

      var rfqJsonString = rfq.toString()
      val rfqJsonNode = Json.jsonMapper.readTree(rfqJsonString) as ObjectNode
      removeNestedJsonNodeProp(rfqJsonNode, "/privateData/salt")
      rfqJsonString = rfqJsonNode.toString()

      assertThrows<IllegalArgumentException> { Rfq.parse(rfqJsonString, requireAllPrivateData = false) }
    }

    @Test
    fun `throws if privateData payin paymentDetails is incorrect but data payin paymentDetailsHash is present`() {
      val rfq = TestData.getRfq()
      rfq.data.payin.paymentDetailsHash = "garbage"
      rfq.sign(TestData.ALICE_DID)

      val rfqJson = rfq.toString()

      assertThrows<IllegalArgumentException> { Rfq.parse(rfqJson, requireAllPrivateData = false) }
    }

    @Test
    fun `throws if privateData payout paymentDetails is incorrect but data payout paymentDetailsHash is present`() {
      val rfq = TestData.getRfq()
      rfq.data.payout.paymentDetailsHash = "garbage"
      rfq.sign(TestData.ALICE_DID)

      val rfqJson = rfq.toString()

      assertThrows<IllegalArgumentException> { Rfq.parse(rfqJson, requireAllPrivateData = false) }
    }

    @Test
    fun `throws if privateData claims is incorrect but data claimsHash is present`() {
      val rfq = TestData.getRfq(claims = listOf(TestData.getVC().toString()))
      rfq.data.claimsHash = "garbage"
      rfq.sign(TestData.ALICE_DID)

      val rfqJson = rfq.toString()

      assertThrows<IllegalArgumentException> { Rfq.parse(rfqJson, requireAllPrivateData = false) }
    }
  }

  fun removeNestedJsonNodeProp(rootNode: ObjectNode, pointerExpression: String) {
    val jsonPointer: JsonPointer = JsonPointer.compile(pointerExpression)
    val parentPath: JsonPointer = jsonPointer.head()
    val lastToken: String = jsonPointer.last().matchingProperty

    val parentNode: JsonNode = rootNode.at(parentPath)
    if (parentNode is ObjectNode) {

      parentNode.remove(lastToken)
        ?: throw Exception("Could not remove field $pointerExpression because it does not exist")
    } else {
      throw Exception("Could not remove field $pointerExpression from JSON object")
    }
  }
}


