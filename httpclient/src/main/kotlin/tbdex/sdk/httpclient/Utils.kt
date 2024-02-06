package tbdex.sdk.httpclient

import web5.sdk.dids.DidResolvers

/**
 * Get pfi service endpoint
 *
 * @param pfiDid
 * @return
 */
fun getPfiServiceEndpoint(pfiDid: String): String {
  val didResolutionResult = DidResolvers.resolve(pfiDid)
  val service = didResolutionResult.didDocument?.services?.find { it.isType("PFI") }

  requireNotNull(service) {
    "DID does not have service of type PFI"
  }

  return service.serviceEndpoint.toString()
}