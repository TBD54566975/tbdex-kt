package tbdex.sdk.httpserver.models

import tbdex.sdk.protocol.models.Rfq

/**
 * Data class used to type request body to create exchange via TbdexHttpServer.
 *
 * @property rfq Rfq tbdex message received.
 * @property replyTo Optional URL to be included in the request to create exchange.
 * @constructor Create empty Create exchange request
 */
class CreateExchangeRequest(
  val rfq: Rfq,
  val replyTo: String? = null
)