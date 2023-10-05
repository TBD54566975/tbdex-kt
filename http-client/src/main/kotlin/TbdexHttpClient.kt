import models.Message
import models.MessageKind
import java.util.Date

class SendMessageOptions(
  val message: Message
)

class TbdexHttpClient {

  companion object {
    // todo: add MessageKind generic typing?
    fun sendMessage(opt: SendMessageOptions) : Any {

      val message = opt.message
      Message.verify(message)
      val toSign = Pair(message.metadata, message.data)
      val hashed = "" // Crypto.hash(toSign)
      val signer = TODO() // Crypto.verify(hashed, message.signature)
      if (message.metadata.from != signer) {
        throw Exception("Signature verification failed: Exepcted DID in kid of JWS header must match metadata.from")
      }
      return signer
    }

    fun generateRequestToken(): String {

      val timestampToken = Date().toString()
      return "" // Crypto.sign({privateKeyJwk, kid, payload: timestampToken})

    }
  }
}