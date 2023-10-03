package models

import typeid.TypeID
import java.time.OffsetDateTime

class OrderStatusData(val status: String) : MessageData

class OrderStatus private constructor(
    metadata: MessageMetadata,
    data: OrderStatusData,
    signature: String? = null
) : Message<OrderStatusData>(metadata, data, signature) {
    companion object {
        fun create(to: String, from: String, exchangeId: TypeID, orderStatusData: OrderStatusData): OrderStatus {
            val metadata = MessageMetadata(
                kind = MessageKind.order,
                to = to,
                from = from,
                id = TypeID(MessageKind.order.name),
                exchangeId = exchangeId,
                createdAt = OffsetDateTime.now()
            )
            return OrderStatus(metadata, orderStatusData)
        }
    }
}