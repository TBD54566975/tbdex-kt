package tbdex.sdk.protocol.models

import assertk.assertAll
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.startsWith
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import tbdex.sdk.protocol.TestData
import tbdex.sdk.protocol.serialization.Json
import kotlin.test.assertIs

class BalanceTest {

  @Test
  fun `can create a new balance`() {
    val balance = Balance.create(
      from = TestData.PFI,
      data = BalanceData(
        currencyCode = "AUD",
        available = "100"
      )
    )

    assertAll {
      assertThat(balance.data.currencyCode).isEqualTo("AUD")
      assertThat(balance.data.available).isEqualTo("100")
      assertThat(balance.metadata.id).startsWith("balance")
      assertThat(balance.metadata.protocol).isEqualTo("1.0")
    }
  }

  @Test
  fun `can parse balance from a json string`() {
    val balance = TestData.getBalance()
    balance.sign(TestData.PFI_DID)
    val jsonResource = balance.toString()
    val parsedBalance = Resource.parse(jsonResource)

    assertIs<Balance>(parsedBalance)
    assertThat(parsedBalance.toString()).isEqualTo(jsonResource)
  }

  @Test
  fun `can parse a balance`() {
    val balance = TestData.getBalance()
    balance.sign(TestData.PFI_DID)

    val parsedBalance = assertDoesNotThrow { Resource.parse(Json.stringify(balance)) }
    assertIs<Balance>(parsedBalance)
  }

  @Test
  fun `can parse a balance explicitly`() {
    val balance = TestData.getBalance()
    balance.sign(TestData.PFI_DID)

    val parsedBalance = assertDoesNotThrow { Balance.parse(Json.stringify(balance)) }
    assertIs<Balance>(parsedBalance)
  }

}