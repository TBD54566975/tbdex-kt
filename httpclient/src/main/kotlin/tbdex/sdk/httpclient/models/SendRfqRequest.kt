package tbdex.sdk.httpclient.models

import tbdex.sdk.protocol.models.Rfq

/**
 * Data class used to format a request to send an RFQ via TbdexHttpClient.
 *
 * @property rfq Rfq tbdex message to be sent.
 * @property replyTo Optional URL to be included in the request.
 */
class SendRfqRequest(
  val rfq: Rfq,
  val replyTo: String? = null
)