package crypto

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.security.MessageDigest
import java.security.PrivateKey
import java.util.Base64


class Crypto {

  companion object {
    private val cborMapper : ObjectMapper = ObjectMapper(CBORFactory())
      .registerKotlinModule()

    fun hash(payload: Any) : String {
      val cborEncodedPayloadBuffer = cborMapper.writeValueAsBytes(payload)
      val sha256CborEncodedPayloadBytes = MessageDigest.getInstance("SHA-256").digest(cborEncodedPayloadBuffer)

      return Base64.getUrlEncoder().withoutPadding().encodeToString(sha256CborEncodedPayloadBytes)

    }

    fun verify(detachedPayload: String?, signature: String?) : String {

      if (signature == null) {
        throw Exception("Signature verification failed: Expected signature property to exist")
      }

      val splitJws = signature.split('.')
      if (splitJws.size != 3) { // ensure that JWS has 3 parts
        throw Exception("Signature verification failed: Expected valid JWS with detached content")
      }

      var (base64UrlEncodedJwsHeader, base64urlEncodedJwsPayload, base64UrlEncodedSignature) = splitJws

      if (detachedPayload != null) {
        if (base64urlEncodedJwsPayload.isNotEmpty()) { // ensure that JWS payload is empty
          throw Exception("Signature verification failed: Expected valid JWS with detached content")
        }
        base64urlEncodedJwsPayload = detachedPayload
      }

      val jwsHeaderByteArray: ByteArray = Base64.getUrlDecoder().decode(base64UrlEncodedJwsHeader)
      val jwsHeaderUtf8String: String = jwsHeaderByteArray.toString(Charsets.UTF_8)
      // TODO: import JwsHeader type from web5-kt
      // todo: the rest of this function
//      val jwsHeader = Json.parse<JwsHeader>(jwsHeaderUtf8String)
//      if (!jwsHeader.alg || !jwsHeader.kid) { // ensure that JWS header has required properties
//        throw new Error('Signature verification failed: Expected JWS header to contain alg and kid')
//      }

      return ""
    }

    fun sign(detachedPayload: String? = null, payload: Any, privateKeyJwk: Any, kid: String): String {

      return ""
    }
  }

}