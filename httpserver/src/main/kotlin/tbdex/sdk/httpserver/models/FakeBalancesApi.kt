package tbdex.sdk.httpserver.models

import tbdex.sdk.protocol.models.Balance
import tbdex.sdk.protocol.models.BalanceData

/**
 * Fake balances api.
 *
 */
class FakeBalancesApi : BalancesApi {

  /**
   * Get balances.
   *
   * @return List of balances.
   */
  override fun getBalances(requesterDid: String): List<Balance> {

    val usdBalance = Balance.create(
      from = "did:ex:pfi",
      data = BalanceData(
        currencyCode = "USD",
        available = "100.00"
      )
    )

    val btcBalance = Balance.create(
      from = "did:ex:pfi",
      data = BalanceData(
        currencyCode = "BTC",
        available = "0.000038"
      )
    )

    return listOf(usdBalance, btcBalance)
  }
}