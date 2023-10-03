package models

import Mapper
import com.fasterxml.jackson.annotation.JsonFormat
import dateTimeFormat
import typeid.TypeID
import java.time.OffsetDateTime

// TODO: linter gonna yell at us for this, but I want the typeid and serialization to be ez for now
enum class MessageKind {
    rfq, order, orderstatus
}

class MessageMetadata(
    val kind: MessageKind,
    val to: String,
    val from: String,
    val id: TypeID,
    val exchangeId: TypeID,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimeFormat, timezone = "UTC")
    val createdAt: OffsetDateTime
)

sealed interface MessageData

abstract class Message<T : Any>(
    val metadata: MessageMetadata,
    val data: T,
    var signature: String? = null
) {
    init {
        when (metadata.kind) {
            MessageKind.rfq -> require(data is RfqData)
            MessageKind.order -> require(data is OrderData)
            MessageKind.orderstatus -> require(data is OrderStatusData)
        }

        if (signature != null) {
            verify()
        } else {
            validate()
        }
    }

    private fun verify() {
        validate()

        // TODO detached payload sig check (regenerate payload and then check)
    }

    private fun validate() {
        // TODO validate against json schema
//    val schema = schemaMap.get(metadata.kind.name)
//    val jsonString = this.toString()
//    schema.validateBasic(jsonString)
//    if (output.errors != null) ...
    }

    // TODO - use web5 crypto and fix the types
    fun sign(privateKey: String, kid: String) {
        this.signature = "blah"
    }

    override fun toString(): String {
        return Mapper.writer().writeValueAsString(this)
    }

    companion object {
        fun parse(payload: String): Any {
            // TODO json schema validation using Message schema

//      val message = Mapper.reader(Message::class.java).readValue<Message<Any>>(payload)
            val node = Mapper.objectMapper.readTree(payload)
            val kind = node.get("metadata").get("kind").asText()

            val kindEnum = MessageKind.valueOf(kind)

            // TODO json schema validation using specific type schema

            return when (kindEnum) {
                MessageKind.rfq -> Mapper.reader(Rfq::class.java).readValue(payload)
                MessageKind.order -> Mapper.reader(Order::class.java).readValue(payload)
                MessageKind.orderstatus -> Mapper.reader(OrderStatus::class.java).readValue(payload)
            }
        }
    }
}



