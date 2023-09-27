package models

data class Message(
  val metadata: MessageMetadata<T>,
  val data: MessageData<T>,
  val signature: String
)
