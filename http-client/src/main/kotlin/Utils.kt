import web5.sdk.common.Convert
import web5.sdk.dids.Did
import web5.sdk.dids.DidResolvers
import java.time.Instant

fun getPfiServiceEndpoint(pfiDid: String): String {
  val didResolutionResult = DidResolvers.resolve(pfiDid)
  val service = didResolutionResult.didDocument.services.find { it.isType("PFI") }

  requireNotNull(service) {
    "DID does not have service of type PFI"
  }

  return service.serviceEndpoint.toString()
}


fun generateRequestToken(did: Did, keyAlias: String? = null): String {
  // TODO drop keyalias logic in here
  val resolvedAlias = ""

  val payload = Instant.now()
  val payloadBytes = Convert(payload).toByteArray()

  val signed = did.keyManager.sign(resolvedAlias, payloadBytes)
  return signed.toString()
}