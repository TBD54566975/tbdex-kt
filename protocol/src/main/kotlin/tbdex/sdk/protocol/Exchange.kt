package tbdex.sdk.protocol

import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.MessageKind
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.OrderStatus
import tbdex.sdk.protocol.models.Quote
import tbdex.sdk.protocol.models.Rfq

class Exchange(
  var rfq: Rfq? = null,
  var quote: Quote? = null,
  var order: Order? = null,
  var orderStatus: MutableList<OrderStatus> = mutableListOf(),
  var close: Close? = null
) {

  fun addMessages(messages: List<Message>) {

    val sortedMessages = messages.sortedBy { it.metadata.createdAt }

    for (message in sortedMessages) {
      this.addNextMessage(message)
    }
  }

  fun addNextMessage(message: Message) {
    if (!this.isValidNext(message.metadata.kind)) {
      throw InvalidNextMessageException(
        "Could not add message ${message.metadata.id} to Exchange because" +
          "${message.metadata.kind} is not a valid next message for this Exchange"
      )
    }

    if (this.exchangeId() != null &&
      message.metadata.exchangeId.toString() != this.exchangeId()) {
      throw ExchangeIdNotFoundException(
        "Could not add message ${message.metadata.id} to Exchange because" +
          "it does not have matching exchange id ${this.exchangeId()}"
      )
    }

    when (message.metadata.kind) {
      MessageKind.rfq -> this.rfq = message as Rfq
      MessageKind.quote -> this.quote = message as Quote
      MessageKind.order -> this.order = message as Order
      MessageKind.orderstatus -> {
        this.orderStatus.add(message as OrderStatus)
      }

      MessageKind.close -> this.close = message as Close
      else -> throw UnknownMessageKindException("Unrecognized message kind")
    }

  }

  fun isValidNext(kind: MessageKind): Boolean {
    val validNext = this.latestMessage()?.validNext ?: setOf(MessageKind.rfq)
    return validNext.contains(kind)
  }

  fun latestMessage(): Message? {
    return when {
      this.close != null -> this.close
      this.orderStatus.isNotEmpty() -> this.orderStatus.last()
      this.order != null -> this.order
      this.quote != null -> this.quote
      this.rfq != null -> this.rfq
      else -> null
    }
  }

  fun exchangeId(): String? {
    return this.rfq?.metadata?.exchangeId?.toString()
  }

  fun messages(): List<Message> {
    return listOfNotNull(this.rfq, this.quote, this.order, this.close) +
      (this.orderStatus)
  }

}