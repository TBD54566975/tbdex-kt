package tbdex.sdk.protocol

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.OrderStatus
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class MessagesVectorTest {
  @Test
  fun `parse-close json`() {
    val vector = TestVectors.getVector("parse-rfq.json")
    assertNotNull(vector)

    // `input` is stringified JSON, which we must parse separately
    val input = vector["input"].textValue()
    assertNotNull(input)

    val tbDEXMessage = Message.parse(input)
    assertIs<Rfq>(tbDEXMessage)
  }
}