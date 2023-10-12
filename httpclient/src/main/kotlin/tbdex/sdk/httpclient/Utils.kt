package tbdex.sdk.httpclient

import com.nimbusds.jose.jwk.JWK
import foundation.identity.did.VerificationMethod
import web5.sdk.common.Convert
import web5.sdk.dids.Did
import web5.sdk.dids.DidResolvers
import java.security.SignatureException
import java.time.Instant

fun getPfiServiceEndpoint(pfiDid: String): String {
  val didResolutionResult = DidResolvers.resolve(pfiDid)
  val service = didResolutionResult.didDocument.services.find { it.isType("PFI") }

  requireNotNull(service) {
    "DID does not have service of type PFI"
  }

  return service.serviceEndpoint.toString()
}


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


  val payload = Instant.now()
  val payloadBytes = Convert(payload).toByteArray()

  val signed = did.keyManager.sign(keyAlias, payloadBytes)
  return signed.toString()
}