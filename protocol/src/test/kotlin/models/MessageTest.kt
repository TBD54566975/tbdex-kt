package protocol.models

import models.Message
import protocol.TestData
import kotlin.test.Test

class MessageTest {
  @Test
  fun `can create a list of messages with multiple types`() {
    val rfq = TestData.getRfq()
    val order = TestData.getOrder()
    val exchange: List<Message> = listOf(rfq, order)
  }
}