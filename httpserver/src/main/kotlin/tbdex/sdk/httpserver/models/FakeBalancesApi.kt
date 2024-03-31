package tbdex.sdk.httpserver.models

import tbdex.sdk.protocol.models.Balance

/**
 * Fake balances api.
 *
 */
class FakeBalancesApi : BalancesApi {

  val balances = mutableListOf<Balance>()
  /**
   * Get balances.
   *
   * @return List of balances.
   */
  override fun getBalances(requesterDid: String): List<Balance> {
    return balances
  }

  /**
   * Adds a new balance.
   *
   * @param balance to be added to in memory list of balances
   */
  fun addBalance(balance: Balance) {
    balances.add(balance)
  }

}