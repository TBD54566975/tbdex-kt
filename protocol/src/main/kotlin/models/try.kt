package models

import typeid.TypeID
import java.time.OffsetDateTime

// Abstract Message class with generics
sealed class Payload
class IntPayload(val value: Int) : Payload()
class StringPayload(val value: String) : Payload()

// Abstract Message class with generics
abstract class Message<TPayload : Payload, TData : Any> {
  abstract val payload: TPayload // Abstract property

  abstract fun create(data: TData): Message<TPayload, TData>
}

// Rfq class, a subclass of Message
class Rfq private constructor(private val rfqData: Int) : Message<IntPayload, Int>() {
  override val payload: IntPayload = IntPayload(rfqData)

  companion object {
    fun create(data: Int): Rfq {
      // Parsing and creating an Rfq message
      return Rfq(data)
    }
  }
}

// Quote class, a subclass of Message
class Quote private constructor(private val quoteData: String) : Message<StringPayload, String>() {
  override val payload: StringPayload = StringPayload(quoteData)

  companion object {
    fun create(data: String): Quote {
      // Parsing and creating a Quote message
      return Quote(data)
    }
  }
}

fun main() {
  val rfqMessage = Rfq.create(42)
  val quoteMessage = Quote.create("42.5")

  println("RFQ Payload: ${rfqMessage.payload.value}")
  println("Quote Payload: ${quoteMessage.payload.value}")
}



