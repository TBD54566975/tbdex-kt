package tbdex.sdk.protocol

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.util.Base64URL
import foundation.identity.did.DIDURL
import foundation.identity.did.VerificationMethod
import tbdex.sdk.protocol.Cbor.cborMapper
import web5.sdk.common.Convert
import web5.sdk.crypto.Crypto
import web5.sdk.dids.Did
import web5.sdk.dids.DidResolvers
import java.security.MessageDigest
import java.security.SignatureException

/**
 * Utility functions for cryptographic operations and DID (Decentralized Identifier) handling.
 */
object CryptoUtils {
  /**
   * Hashes the provided payload using SHA-256.
   *
   * @param payload The payload to be hashed.
   * @return The Base64URL-encoded hash of the payload.
   */
  fun hash(payload: Any): Base64URL {
    val cborEncodedPayloadBuffer: ByteArray = cborMapper.writeValueAsBytes(payload)
    val sha256CborEncodedPayloadBytes: ByteArray = MessageDigest.getInstance("SHA-256").digest(cborEncodedPayloadBuffer)
    return Base64URL(Convert(sha256CborEncodedPayloadBytes).toBase64Url(padding = false))
  }

  /**
   * Verifies a detached signature against the provided payload.
   *
   * @param detachedPayload The detached payload to verify.
   * @param signature The signature to verify.
   * @throws IllegalArgumentException if the signature is missing.
   * @throws SignatureException if the verification fails.
   */
  fun verify(detachedPayload: Base64URL?, signature: String?) {
    require(signature != null) {
      throw IllegalArgumentException("Signature verification failed: Expected signature property to exist")
    }

    val jws = JWSObject.parse(signature, Payload(detachedPayload))
    require(jws.header.algorithm != null && jws.header.keyID != null) {
      "Signature verification failed: Expected JWS header to contain alg and kid"
    }

    val verificationMethodId = jws.header.keyID
    val parsedDidUrl = DIDURL.fromString(verificationMethodId) // validates vm id which is a DID URL

    val didResolutionResult = DidResolvers.resolve(parsedDidUrl.did.didString)
    if (didResolutionResult.didResolutionMetadata.error != null) {
      throw SignatureException(
        "Signature verification failed: " +
          "Failed to resolve DID ${parsedDidUrl.did.didString}. " +
          "Error: ${didResolutionResult.didResolutionMetadata.error}"
      )
    }

    // Create a set of possible id matches. The DID spec allows for an id to be the entire `did#fragment`
    // or just `#fragment`. See: https://www.w3.org/TR/did-core/#relative-did-urls.
    // Using a set for fast string comparison. DIDs can be long.
    val verificationMethodIds = setOf(parsedDidUrl.didUrlString, "#${parsedDidUrl.fragment}")
    val assertionMethods = didResolutionResult.didDocument.assertionMethodVerificationMethodsDereferenced
    var assertionMethod: VerificationMethod? = null

    for (method in assertionMethods) {
      val id = method.id.toString()
      if (verificationMethodIds.contains(id)) {
        assertionMethod = method
        break
      }
    }

    if (assertionMethod == null) {
      throw SignatureException(
        "Signature verification failed: Expected kid in JWS header to dereference " +
          "a DID Document Verification Method with an Assertion verification relationship"
      )
    }

    require(assertionMethod.isType("JsonWebKey2020") && assertionMethod.publicKeyJwk != null) {
      throw SignatureException(
        "Signature verification failed: Expected kid in JWS header to dereference " +
          "a DID Document Verification Method of type JsonWebKey2020 with a publicKeyJwk"
      )
    }

    val publicKeyMap = assertionMethod.publicKeyJwk
    val publicKeyJwk = JWK.parse(publicKeyMap)

    Crypto.verify(
      publicKey = publicKeyJwk,
      signedPayload = jws.signingInput,
      signature = jws.signature.decode(),
      algorithm = jws.header.algorithm
    )
  }

  /**
   * Signs the provided payload using the specified DID and key.
   *
   * @param did The DID to use for signing.
   * @param payload The payload to sign.
   * @param assertionMethodId The alias of the key to be used for signing (optional).
   * @return The signed payload as a detached payload JWT (JSON Web Token).
   */
  fun sign(did: Did, payload: Any, assertionMethodId: String? = null): String {
    val didResolutionResult = DidResolvers.resolve(did.uri)
    val assertionMethods = didResolutionResult.didDocument.assertionMethodVerificationMethodsDereferenced

    val assertionMethod: VerificationMethod = when {
      assertionMethodId != null -> assertionMethods.find { it.id.toString() == assertionMethodId }
      else -> assertionMethods.firstOrNull()
    } ?: throw SignatureException("assertion method $assertionMethodId not found")

    // TODO: ensure that publicKeyJwk is not null
    val publicKeyJwk = JWK.parse(assertionMethod.publicKeyJwk)
    val keyAlias = did.keyManager.getDeterministicAlias(publicKeyJwk)

    val publicKey = did.keyManager.getPublicKey(keyAlias)
    val algorithm = publicKey.algorithm
    val jwsAlgorithm = JWSAlgorithm.parse(algorithm.toString())

    val jwsHeader = JWSHeader.Builder(jwsAlgorithm)
      .keyID(assertionMethod.id.toString())
      .build()

    // Create payload
    val base64UrlHashedPayload = hash(payload)
    val jwsPayload = Payload(base64UrlHashedPayload)

    val jwsObject = JWSObject(jwsHeader, jwsPayload)
    val toSign = jwsObject.signingInput

    val signatureBytes = did.keyManager.sign(keyAlias, toSign)

    val base64UrlEncodedSignature = Base64URL(Convert(signatureBytes).toBase64Url(padding = false))
    val base64UrlEncodedHeader = jwsHeader.toBase64URL()

    return "$base64UrlEncodedHeader..$base64UrlEncodedSignature"
  }
}
