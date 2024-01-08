package tbdex.sdk.httpclient

import com.github.f4b6a3.uuid.UuidCreator
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.util.Base64URL
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import foundation.identity.did.VerificationMethod
import web5.sdk.common.Convert
import web5.sdk.dids.Did
import web5.sdk.dids.DidResolvers
import web5.sdk.dids.findAssertionMethodById
import java.time.Instant
import java.util.Date

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
 * @param did DID of the token creator
 * @param pfiDid DID of the PFI
 * @param assertionMethodId
 * @return
 */
fun generateRequestToken(did: Did, pfiDid: String, assertionMethodId: String? = null): String {

  val didResolutionResult = DidResolvers.resolve(did.uri)
  val assertionMethod: VerificationMethod = didResolutionResult.didDocument.findAssertionMethodById(assertionMethodId)

  // TODO: ensure that publicKeyJwk is not null
  val publicKeyJwk = JWK.parse(assertionMethod.publicKeyJwk)
  val keyAlias = did.keyManager.getDeterministicAlias(publicKeyJwk)

  // TODO: figure out how to make more reliable since algorithm is technically not a required property of a JWK
  val algorithm = publicKeyJwk.algorithm
  val jwsAlgorithm = JWSAlgorithm.parse(algorithm.toString())

  val kid = when (assertionMethod.id.isAbsolute) {
    true -> assertionMethod.id.toString()
    false -> "${did.uri}${assertionMethod.id}"
  }

  val jwtHeader = JWSHeader.Builder(jwsAlgorithm)
    .type(JOSEObjectType.JWT)
    .keyID(kid)
    .build()

  val now = Instant.now()
  val exp = now.plusSeconds(60)
  val jwtPayload = JWTClaimsSet.Builder()
    .audience(pfiDid)
    .issuer(did.uri)
    .expirationTime(Date.from(exp))
    .issueTime(Date.from(now))
    .jwtID(UuidCreator.getTimeOrderedEpoch().toString())
    .build()

  val jwtObject = SignedJWT(jwtHeader, jwtPayload)
  val toSign = jwtObject.signingInput
  val signatureBytes = did.keyManager.sign(keyAlias, toSign)

  val base64UrlEncodedHeader = jwtHeader.toBase64URL()
  val base64UrlEncodedPayload = jwtPayload.toPayload().toBase64URL()
  val base64UrlEncodedSignature = Base64URL(Convert(signatureBytes).toBase64Url(padding = false))

  return "$base64UrlEncodedHeader.$base64UrlEncodedPayload.$base64UrlEncodedSignature"
}