package tbdex.server.tbdex.server.requestHandlers

import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Rfq
import tbdex.server.SubmitCallback

// todo write something similar to submitRfq request handler in tbdex-js
// validate the message and then invoke submitCallback
fun submitRfq(exchangeId: String, message: String, submitCallback: SubmitCallback) {

  val message = Message.parse(message) as Rfq

}