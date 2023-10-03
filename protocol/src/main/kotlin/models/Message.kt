package models

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import typeid.TypeID
import java.time.OffsetDateTime
import java.time.ZoneOffset

// TODO we don't really need this rn, but is one method of creating a common type that can be used in a hypothetical base class
sealed class MessageData
class RfqData(val amount: Int) : MessageData()
class OrderData : MessageData()

enum class MessageKind {
  rfq, order, orderstatus
}

const val dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"

class MessageMetadata(
  val kind: MessageKind,
  val to: String,
  val from: String,
  val id: TypeID,
  val exchangeId: TypeID,
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimeFormat, timezone = "UTC")
  val createdAt: OffsetDateTime
)

// TODO signature and sign seem ripe to be abstracted into a base class
class Rfq private constructor(val data: RfqData, val metadata: MessageMetadata, var signature: String? = null) {
  // TODO JANKKKK to have two mappers in the same class
  private val mapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .findAndRegisterModules()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

  // TODO - use web5 crypto and fix the types
  fun sign(privateKey: String, kid: String) {
    this.signature = "blah"
  }

  override fun toString(): String {
    return mapper.writeValueAsString(this)
  }

  companion object {
    // TODO is it best practice to share a mapper across different classes?
    private val mapper: ObjectMapper = ObjectMapper()
      .registerKotlinModule()
      .findAndRegisterModules()
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    fun create(to: String, from: String, amount: Int): Rfq {
      println(MessageKind.rfq)
      val id = TypeID(MessageKind.rfq.name)
      val metadata = MessageMetadata(
        kind = MessageKind.rfq,
        to = to,
        from = from,
        id = id,
        exchangeId = id,
        createdAt = OffsetDateTime.now(ZoneOffset.UTC)
      )

      val data = RfqData(amount)
      return Rfq(data, metadata)
    }

    fun parse(data: String): Rfq {
      // TODO verify the signature
      // TODO verify against json schemas

      // TODO not validated, do we need to read the subtypes individually? (metadata and data)
      return mapper.readValue<Rfq>(data)
    }
  }
}

class Order private constructor(val data: OrderData, val metadata: MessageMetadata, var signature: String? = null) {
  // TODO - use web5 crypto and fix the types
  fun sign(privateKey: String, kid: String) {
    this.signature = "blah"
  }

  companion object {
    // TODO is it best practice to share a mapper across different classes?
    private val mapper: ObjectMapper = ObjectMapper()
      .registerKotlinModule()

    fun create(to: String, from: String, exchangeId: TypeID): Order {
      val metadata = MessageMetadata(
        kind = MessageKind.order,
        to = to,
        from = from,
        id = TypeID(MessageKind.order.name),
        exchangeId = exchangeId,
        createdAt = OffsetDateTime.now()
      )
      return Order(OrderData(), metadata)
    }

    fun parse(data: String): Order {
      // TODO verify the signature
      // TODO verify against json schemas

      return mapper.readValue<Order>(data)
    }
  }
}

// TODO add the other message types - haven't done it while we figure out the structure/interface we want

fun main() {
  val rfqMessage = Rfq.create("pfi", "alice", 20)
  println("RfqMessage: $rfqMessage")

  val orderMessage = Order.create("pfi", "alice", rfqMessage.metadata.exchangeId)
  println("OrderMessage: $orderMessage")

  // TODO add example usage of parse
}



