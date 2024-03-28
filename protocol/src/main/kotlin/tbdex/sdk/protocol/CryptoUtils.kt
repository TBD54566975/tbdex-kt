package tbdex.sdk.protocol

import org.erdtman.jcs.JsonCanonicalizer
import tbdex.sdk.protocol.models.Data
import tbdex.sdk.protocol.models.Metadata
import tbdex.sdk.protocol.serialization.Json
import web5.sdk.common.Convert
import web5.sdk.common.EncodingFormat
import web5.sdk.dids.didcore.Did
import web5.sdk.jose.jws.DecodedJws
import web5.sdk.jose.jws.JwsHeader
import java.security.MessageDigest
import java.security.SignatureException

/**
 * Utility functions for cryptographic operations and DID (Decentralized Identifier) handling.
 */
object CryptoUtils {
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
   * @throws SignatureException if the verification fails.
   */
  fun verify(detachedPayload: ByteArray, signature: String, did: String) {
    val decodedJws = jwsDecode(signature, detachedPayload)

    require(decodedJws.header.kid != null) {
      "Signature verification failed: Expected JWS header to contain alg and kid"
    }

    val verificationMethodId = decodedJws.header.kid
    val parsedDid = Did.parse(verificationMethodId!!)

    val signingDid = parsedDid.uri
    if (signingDid != did) {
      throw SignatureException(
        "Signature verification failed: Was not signed by the expected DID. " +
          "Expected DID: $did. Actual DID: $signingDid"
      )
    }

    decodedJws.verify()
  }

  @Suppress("SwallowedException")
  private fun jwsDecode(sigWithoutPayload: String, detachedPayload: ByteArray): DecodedJws {
    val parts = sigWithoutPayload.split(".").toMutableList()
    check(parts.size == 3) {
      "Malformed JWT. Expected 3 parts, got ${parts.size}"
    }

    val header: JwsHeader
    try {
      header = JwsHeader.fromBase64Url(parts[0])
    } catch (e: Exception) {
      throw SignatureException("Malformed JWT. Failed to decode header: ${e.message}")
    }

    val part1 = Convert(detachedPayload).toBase64Url()
    parts[1] = part1
    val signature: ByteArray
    try {
      signature = Convert(parts[2], EncodingFormat.Base64Url).toByteArray()
    } catch (e: Exception) {
      throw SignatureException("Malformed JWT. Failed to decode signature: ${e.message}")
    }

    return DecodedJws(header, detachedPayload, signature, parts)
  }
}
