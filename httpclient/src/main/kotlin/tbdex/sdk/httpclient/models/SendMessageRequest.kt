package tbdex.sdk.httpclient.models

import tbdex.sdk.protocol.models.Message

/**
 * Data class used to format a request to send a tbdex message via TbdexHttpClient.
 *
 * @property message Tbdex message to be sent.
 * @property replyTo Optional URL to be included in the request when sending a Rfq Message.
 */

class SendMessageRequest(
  val message: Message,
  val replyTo: String? = null
)