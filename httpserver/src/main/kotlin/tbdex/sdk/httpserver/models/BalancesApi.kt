package tbdex.sdk.httpserver.models

import tbdex.sdk.protocol.models.Balance

/**
 * Interface representing an API for interacting with TBDex balances and related entities.
 */
interface BalancesApi {

  /**
   * Retrieves a list of balances based on the provided filter.
   *
   * @param requesterDid The DID of the requester.
   * @return A list of [Balance] representing balances based on the filter.
   */
  fun getBalances(requesterDid: String): List<Balance>

}