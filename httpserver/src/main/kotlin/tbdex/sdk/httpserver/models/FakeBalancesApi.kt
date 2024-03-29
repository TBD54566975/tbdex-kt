package tbdex.sdk.httpserver.models

import tbdex.sdk.protocol.models.Balance
import tbdex.sdk.protocol.models.BalanceData

class FakeBalancesApi : BalancesApi {

  override fun getBalances(): List<Balance> {

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