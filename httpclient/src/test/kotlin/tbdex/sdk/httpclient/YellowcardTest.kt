package tbdex.sdk.httpclient

import org.junit.jupiter.api.Disabled
import tbdex.sdk.httpclient.models.GetOfferingsResponse
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.models.RfqData
import tbdex.sdk.protocol.models.SelectedPaymentMethod
import kotlin.test.Test

class YellowCardTest {
  @Suppress("MaximumLineLength")
  private val ionDid =
    "did:ion:EiDCYKaMtz8hWnylrPKaDsOqNoM973GWqfGCUIeLQesWcg:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJkd24tc2lnIiwicHVibGljS2V5SndrIjp7ImNydiI6IkVkMjU1MTkiLCJrdHkiOiJPS1AiLCJ4IjoidGdXWUF3ajlSRkhXaEJON2Fya0pnQTJKSUlDbHg2Zm54cjVjeE9jNm95SSJ9LCJwdXJwb3NlcyI6WyJhdXRoZW50aWNhdGlvbiJdLCJ0eXBlIjoiSnNvbldlYktleTIwMjAifV0sInNlcnZpY2VzIjpbeyJpZCI6InBmaSIsInNlcnZpY2VFbmRwb2ludCI6Imh0dHBzOi8vcGZpLnllbGxvd2NhcmQuZW5naW5lZXJpbmciLCJ0eXBlIjoiUEZJIn1dfX1dLCJ1cGRhdGVDb21taXRtZW50IjoiRWlCSk9ha3M4WmI2LXJueDdzMERnWnZqel9YS3NfUEJoN3BTcUgycUQzMXphQSJ9LCJzdWZmaXhEYXRhIjp7ImRlbHRhSGFzaCI6IkVpQWQxRTRSWVBEdlUtTUNlZnY3cUZUOEszaTVZcjNrZ3BnOWhiSkhsWXg0ZnciLCJyZWNvdmVyeUNvbW1pdG1lbnQiOiJFaUNXYzVzekFiWUpsMzVWci1Sdzl6ZE1hWDNlaGZPQUlBUHhEVnhsY3NjWWZBIn19"

  @Test
  @Disabled
  fun `can get offerings`() {
    val resp = RealTbdexClient.getOfferings(ionDid) as GetOfferingsResponse
    // TODO assert response
  }

  @Test
  @Disabled
  fun `can send an RFQ`() {
    val resp = RealTbdexClient.getOfferings(ionDid) as GetOfferingsResponse
    val offering = resp.data[2] as Offering
    val message = Rfq.create(
      to = ionDid,
      from = TestData.ALICE_DID.uri,
      rfqData = RfqData(
        offering.metadata.id,
        "200",
        SelectedPaymentMethod("KES_ADDRESS"),
        SelectedPaymentMethod("MOMO_MPESA"),
        listOf()
      )
    )
    message.sign(TestData.ALICE_DID)
    val rfqResp = RealTbdexClient.sendMessage(message)
    // TODO assert response
  }

  @Test
  @Disabled
  fun `can get an exchange`() {
    TODO()
  }
}