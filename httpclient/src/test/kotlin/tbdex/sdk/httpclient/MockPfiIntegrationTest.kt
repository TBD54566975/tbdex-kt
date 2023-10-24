package tbdex.sdk.httpclient

import GetOfferingsResponse
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.models.RfqData
import tbdex.sdk.protocol.models.SelectedPaymentMethod
import web5.sdk.credentials.VerifiableCredential
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.DidKey
import kotlin.test.Test

/**
 * mock pfi integration test
 *
 * @constructor Create empty mock pfi integration test
 */
class MockPfiIntegrationTest {
  private val issuerDid = DidKey.create(InMemoryKeyManager())
  private val did = DidKey.create(InMemoryKeyManager())
  private val ycDid = "did:ion:EiDHp32_Oud2M2CM2DHO8rNoNSDFOyl9kHf9BwN-Ci6bsQ:eyJkZWx0YSI6eyJwYXRjaGVzIjpbeyJhY3Rpb24iOiJyZXBsYWNlIiwiZG9jdW1lbnQiOnsicHVibGljS2V5cyI6W3siaWQiOiJkd24tc2lnIiwicHVibGljS2V5SndrIjp7ImNydiI6IkVkMjU1MTkiLCJrdHkiOiJPS1AiLCJ4IjoiVGtTZFUtWWd6YlJyQnpPeEJSb0g4MWNPaTJQd3dFSnE3UVotNy1RYkh0ayJ9LCJwdXJwb3NlcyI6WyJhdXRoZW50aWNhdGlvbiJdLCJ0eXBlIjoiSnNvbldlYktleTIwMjAifV0sInNlcnZpY2VzIjpbeyJpZCI6InBmaSIsInNlcnZpY2VFbmRwb2ludCI6Imh0dHA6Ly9sb2NhbGhvc3Q6OTAwMCIsInR5cGUiOiJQRkkifV19fV0sInVwZGF0ZUNvbW1pdG1lbnQiOiJFaUFCME5MQlg4bG11TlFOT0MzUW9tZ2x6bEVBOTd3YzlFRUZnYkVKNEEzUzRRIn0sInN1ZmZpeERhdGEiOnsiZGVsdGFIYXNoIjoiRWlDZEltSC1jaUtxTnkyZjFQNGd0aDlycDBLY2REaGVkcXlpXzVRYVc3eml1USIsInJlY292ZXJ5Q29tbWl0bWVudCI6IkVpQVRiX2M3eTl4LTJNejFYa0JkVnZhdlZOWHp1NEZWeTczekw1enNtOWFWR1EifX0"

  @Test
  fun `can integrate`() {
    println(issuerDid.uri)
    val offeringResponse = (RealTbdexClient.getOfferings(ycDid) as GetOfferingsResponse).data as List<Offering>

    val vc = VerifiableCredential.create<Any>(
      type = "SanctionCredential",
      issuer = issuerDid.uri,
      subject = did.uri,
      data = mapOf("beep" to "boop")
    )
    val claim = vc.sign(issuerDid)
    val rfqMessage = Rfq.create(
      to = ycDid,
      from = did.uri,
      rfqData = RfqData(
        offeringId = offeringResponse[0].metadata.id,
        payinSubunits = "100",
        payinMethod = SelectedPaymentMethod(
          kind = "DIDPAY_BALANCE",
          paymentDetails = emptyMap()
        ),
        payoutMethod = SelectedPaymentMethod(
          kind = "MOMO",
          paymentDetails = emptyMap()
        ),
        claims = listOf(claim)
      )
    )
    rfqMessage.sign(did)
//    println(rfqMessage)

    try {
      val rfqResponse = RealTbdexClient.sendMessage(rfqMessage)
      println(rfqResponse)
    } catch (err: Exception) {
      println(err)
    }
  }
}