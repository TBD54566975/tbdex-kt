package tbdex.sdk.protocol

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.jwk.JWK
import foundation.identity.did.DIDURL
import foundation.identity.did.VerificationMethod
import org.erdtman.jcs.JsonCanonicalizer
import tbdex.sdk.protocol.models.Data
import tbdex.sdk.protocol.models.Metadata
import tbdex.sdk.protocol.serialization.Json
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
  fun verify(detachedPayload: ByteArray, signature: String?, did: String) {
    require(signature != null) {
      throw IllegalArgumentException("Signature verification failed: Expected signature property to exist")
    }

    val jws = JWSObject.parse(signature, Payload(detachedPayload))
    require(jws.header.algorithm != null && jws.header.keyID != null) {
      "Signature verification failed: Expected JWS header to contain alg and kid"
    }

    val verificationMethodId = jws.header.keyID
    val parsedDidUrl = DIDURL.fromString(verificationMethodId) // validates vm id which is a DID URL

    val signingDid = parsedDidUrl.uriWithoutFragment.toString()
    if (signingDid != did) {
      throw SignatureException(
        "Signature verification failed: Was not signed by the expected DID. " +
          "Expected DID: $did. Actual DID: $signingDid"
      )
    }

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
    val assertionMethods = didResolutionResult.didDocument?.assertionMethodVerificationMethodsDereferenced
    val assertionMethod = assertionMethods?.firstOrNull {
      val id = it.id.toString()
      verificationMethodIds.contains(id)
    }

    require(assertionMethod != null) {
      throw SignatureException(
        "Signature verification failed: Expected kid in JWS header to dereference " +
          "a DID Document Verification Method with an Assertion verification relationship"
      )
    }

    require(
      (assertionMethod.isType("JsonWebKey2020") || assertionMethod.isType("JsonWebKey"))
        && assertionMethod.publicKeyJwk != null
    ) {
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
      signature = jws.signature.decode()
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
  fun sign(did: Did, payload: ByteArray, assertionMethodId: String? = null): String {
    val assertionMethod = getAssertionMethod(did, assertionMethodId)

    // TODO: ensure that publicKeyJwk is not null
    val publicKeyJwk = JWK.parse(assertionMethod.publicKeyJwk)
    val keyAlias = did.keyManager.getDeterministicAlias(publicKeyJwk)

    val publicKey = did.keyManager.getPublicKey(keyAlias)
    val algorithm = publicKey.algorithm
    val jwsAlgorithm = JWSAlgorithm.parse(algorithm.toString())

    val selectedAssertionMethodId = when {
      assertionMethod.id.isAbsolute -> assertionMethod.id.toString()
      else -> "${did.uri}${assertionMethod.id}"
    }

    val jwsHeader = JWSHeader.Builder(jwsAlgorithm)
      .keyID(selectedAssertionMethodId)
      .build()

    // Create payload
    val jwsPayload = Payload(payload)
    val jwsObject = JWSObject(jwsHeader, jwsPayload)

    val toSign = jwsObject.signingInput
    val signatureBytes = did.keyManager.sign(keyAlias, toSign)

    val base64UrlEncodedSignature = Convert(signatureBytes).toBase64Url(padding = false)
    val base64UrlEncodedHeader = jwsHeader.toBase64URL()

    return "$base64UrlEncodedHeader..$base64UrlEncodedSignature"
  }

  /**
   * Retrieves the desired assertion verification method from a DID (Decentralized Identifier) based on the provided
   * assertion method identifier.
   *
   * This function resolves the DID, extracts the assertion methods from the DID Document, and returns the specific
   * assertion verification method associated with the provided `assertionMethodId`, if specified. If
   * `assertionMethodId` is not provided, it returns the first assertion verification method found in the DID Document.
   *
   * @param did The Decentralized Identifier (DID) to retrieve the assertion method from.
   * @param assertionMethodId The identifier of the specific assertion verification method to retrieve (optional).
   * @return The assertion verification method corresponding to the provided `assertionMethodId` or the first assertion
   *         verification method found in the DID Document.
   * @throws SignatureException If the specified `assertionMethodId` is not found in the DID Document.
   */
  fun getAssertionMethod(did: Did, assertionMethodId: String?): VerificationMethod {
    val didResolutionResult = DidResolvers.resolve(did.uri)
    val assertionMethods = didResolutionResult.didDocument?.assertionMethodVerificationMethodsDereferenced

    val assertionMethod: VerificationMethod = when {
      assertionMethodId != null -> assertionMethods?.find { it.id.toString() == assertionMethodId }
      else -> assertionMethods?.firstOrNull()
    } ?: throw SignatureException("assertion method $assertionMethodId not found")

    return assertionMethod
  }
}
