package tbdex.sdk.protocol

import org.erdtman.jcs.JsonCanonicalizer
import tbdex.sdk.protocol.models.Data
import tbdex.sdk.protocol.models.Metadata
import tbdex.sdk.protocol.serialization.Json
import web5.sdk.jose.jws.Jws
import java.security.MessageDigest
import java.security.SignatureException

/**
 * Utility functions for producing digest and verifying signatures.
 */
object SignatureVerifier {
  /**
   * Generates a canonicalized digest of the message/resource for signing or verification.
   *
   * @return The message digest as a byte array.
   */
  fun digestOf(metadata: Metadata, data: Data): ByteArray {
    val payload = mapOf("metadata" to metadata, "data" to data)
    val canonicalJsonSerializedPayload = JsonCanonicalizer(Json.stringify(payload))

    val sha256 = MessageDigest.getInstance("SHA-256")
    return sha256.digest(canonicalJsonSerializedPayload.encodedUTF8)
  }

  /**
   * Verifies a detached signature against the provided payload.
   *
   * @param detachedPayload The detached payload to verify.
   * @param signature The signature to verify.
   * @param did The URI string (without fragment) of the signer's DID to verify
   * @throws IllegalArgumentException if the signature is missing.
   * @throws SignatureException if signed by an unexpected DID.
   */
  fun verify(detachedPayload: ByteArray, signature: String?, did: String) {
    require(signature != null) {
      throw IllegalArgumentException("Signature verification failed: Expected signature property to exist")
    }
    val decodedJws = Jws.decode(signature, detachedPayload)

    val signerDid = decodedJws.signerDid
    if (signerDid != did) {
      throw SignatureException(
        "Signature verification failed: Was not signed by the expected DID. " +
          "Expected DID: $did. Actual DID: $signerDid"
      )
    }

    decodedJws.verify()
  }

}
