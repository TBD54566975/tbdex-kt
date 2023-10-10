import Cbor.cborMapper
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.util.Base64URL
import com.nimbusds.jwt.JWTParser
import com.nimbusds.jwt.SignedJWT
import foundation.identity.did.DIDURL
import foundation.identity.did.VerificationMethod
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
  fun verify(detachedPayload: String?, signature: String?) {
    require(signature != null) {
      throw IllegalArgumentException("Signature verification failed: Expected signature property to exist")
    }

    val jwt = JWTParser.parse(signature) as SignedJWT
    require(jwt.header.algorithm != null && jwt.header.keyID != null) {
      "Signature verification failed: Expected JWS header to contain alg and kid"
    }

    if (detachedPayload != null) {
      require(jwt.payload.toBase64URL().decodeToString() == "") {
        "Signature verification failed: Expected valid JWS with detached content"
      }
    }

    val verificationMethodId = jwt.header.keyID
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

    val signedData = "${jwt.header.toBase64URL()}.$detachedPayload"
    val signedDataBytes = Convert(signedData).toByteArray()
    val signatureBytes = jwt.signature.decode()

    Crypto.verify(publicKeyJwk, signedDataBytes, signatureBytes, jwt.header.algorithm)
  }

  /**
   * Signs the provided payload using the specified DID and key.
   *
   * @param did The DID to use for signing.
   * @param payload The payload to sign.
   * @param keyAlias The alias of the key to be used for signing (optional).
   * @return The signed payload as a detached payload JWT (JSON Web Token).
   */
  fun sign(did: Did, payload: Any, keyAlias: String? = null): String {
    val keyAliasToUse = keyAlias ?: run {
      val didResolutionResult = DidResolvers.resolve(did.uri)
      val verificationMethod = didResolutionResult.didDocument.allVerificationMethods[0]

      require(verificationMethod != null) { "No verification method found" }

      val jwk = JWK.parse(verificationMethod.publicKeyJwk)
      jwk.keyID
    }

    val publicKey = did.keyManager.getPublicKey(keyAliasToUse)
    val algorithm = publicKey.algorithm
    val jwsAlgorithm = JWSAlgorithm.parse(algorithm.toString())

    val jwtHeader = JWSHeader.Builder(jwsAlgorithm)
      .keyID(keyAliasToUse)
      .build()

    // Create payload
    val base64UrlHashedPayload = hash(payload)
    val jwsPayload = Payload(base64UrlHashedPayload)

    val jwsObject = JWSObject(jwtHeader, jwsPayload)
    val toSign = jwsObject.signingInput

    val signatureBytes = did.keyManager.sign(keyAliasToUse, toSign)

    val base64UrlEncodedSignature = Base64URL(Convert(signatureBytes).toBase64Url(padding = false))
    val base64UrlEncodedHeader = jwtHeader.toBase64URL()

    return "$base64UrlEncodedHeader..$base64UrlEncodedSignature"
  }
}
