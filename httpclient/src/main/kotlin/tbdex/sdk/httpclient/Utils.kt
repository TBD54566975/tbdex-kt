package tbdex.sdk.httpclient

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.util.Base64URL
import foundation.identity.did.VerificationMethod
import web5.sdk.common.Convert
import web5.sdk.dids.Did
import web5.sdk.dids.DidResolvers
import java.security.SignatureException
import java.time.Instant

/**
 * Get pfi service endpoint
 *
 * @param pfiDid
 * @return
 */
fun getPfiServiceEndpoint(pfiDid: String): String {
  val didResolutionResult = DidResolvers.resolve(pfiDid)
  val service = didResolutionResult.didDocument.services.find { it.isType("PFI") }

  requireNotNull(service) {
    "DID does not have service of type PFI"
  }

  return service.serviceEndpoint.toString()
}


/**
 * Generate request token.
 *
 * @param did
 * @param assertionMethodId
 * @return
 */
fun generateRequestToken(did: Did, assertionMethodId: String? = null): String {
  val didResolutionResult = DidResolvers.resolve(did.uri)
  val assertionMethods = didResolutionResult.didDocument.assertionMethodVerificationMethodsDereferenced

  val assertionMethod: VerificationMethod = when {
    assertionMethodId != null -> assertionMethods.find { it.id.toString() == assertionMethodId }
    else -> assertionMethods.firstOrNull()
  } ?: throw SignatureException("assertion method $assertionMethodId not found")

  // TODO: ensure that publicKeyJwk is not null
  val publicKeyJwk = JWK.parse(assertionMethod.publicKeyJwk)
  val keyAlias = did.keyManager.getDeterministicAlias(publicKeyJwk)

  val algorithm = publicKeyJwk.algorithm
  val jwsAlgorithm = JWSAlgorithm.parse(algorithm.toString())

  val jwsHeader = JWSHeader.Builder(jwsAlgorithm)
    .keyID(assertionMethod.id.toString())
    .build()

  val payload = mapOf("timestamp" to Instant.now().toString())
  val jwsPayload = Payload(payload)
  val base64UrlEncodedPayload = jwsPayload.toBase64URL().toString()

  val jwsObject = JWSObject(jwsHeader, jwsPayload)
  val toSign = jwsObject.signingInput

  val signedBytes = did.keyManager.sign(keyAlias, toSign)
  val base64UrlEncodedSignature = Base64URL(Convert(signedBytes).toBase64Url(padding = false))
  val base64UrlEncodedHeader = jwsHeader.toBase64URL()


  return "$base64UrlEncodedHeader.$base64UrlEncodedPayload.$base64UrlEncodedSignature"
}