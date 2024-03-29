package tbdex.sdk.httpclient

import com.fasterxml.jackson.module.kotlin.convertValue
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import tbdex.sdk.httpclient.models.CreateExchangeRequest
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.httpclient.models.Exchange
import tbdex.sdk.httpclient.models.GetExchangesFilter
import tbdex.sdk.httpclient.models.GetOfferingsFilter
import tbdex.sdk.httpclient.models.TbdexResponseException
import tbdex.sdk.protocol.Validator
import tbdex.sdk.protocol.models.Balance
import tbdex.sdk.protocol.models.Close
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Offering
import tbdex.sdk.protocol.models.Order
import tbdex.sdk.protocol.models.Rfq
import tbdex.sdk.protocol.serialization.Json
import tbdex.sdk.protocol.serialization.Json.jsonMapper
import web5.sdk.dids.did.BearerDid

/**
 * A client of the tbDEX HTTP interface for communicating with a PFI.
 */
object TbdexHttpClient {
  private val client = OkHttpClient()
  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
  private const val JSON_HEADER = "application/json"

  /**
   * Fetches offerings from a PFI.
   *
   * @param pfiDid The decentralized identifier of the PFI.
   * @param filter An optional filter to apply for fetching offerings. If null, all offerings for the given PFI will be
   * fetched.
   * @return A list of [Offering] matching the request.
   * @throws TbdexResponseException for request or response errors.
   */
  fun getOfferings(pfiDid: String, filter: GetOfferingsFilter? = null): List<Offering> {
    val pfiServiceEndpoint = getPfiServiceEndpoint(pfiDid)
    val baseUrl = "$pfiServiceEndpoint/offerings/"

    // compose query param
    val queryMap: Map<String, String?>? = filter?.let { jsonMapper.convertValue(it) }
    val notNullQueryMap = queryMap?.filterValues { it != null }
    val httpUrlBuilder = baseUrl.toHttpUrl().newBuilder()
    notNullQueryMap?.forEach { httpUrlBuilder.addQueryParameter(it.key, it.value) }

    val request = Request.Builder()
      .url(httpUrlBuilder.build())
      .get()
      .build()

    val response: Response = client.newCall(request).execute()
    when {
      response.isSuccessful -> {
        val responseString = response.body?.string()
        // response body is an object with a data field
        val jsonNode = jsonMapper.readTree(responseString)
        return jsonNode.get("data").elements()
          .asSequence()
          .map { Offering.parse(it.toString()) }
          .toList()
      }

      else -> throw buildResponseException(response)
    }
  }

  /**
   * Fetches balances from a PFI.
   *
   * @param pfiDid The decentralized identifier of the PFI.
   * @param requesterDid The decentralized identifier of the entity requesting the balances.
   * @return A list of [Balance] matching the request.
   * @throws TbdexResponseException for request or response errors.
   */
  fun getBalances(pfiDid: String, requesterDid: BearerDid): List<Balance> {
    val pfiServiceEndpoint = getPfiServiceEndpoint(pfiDid)
    val baseUrl = "$pfiServiceEndpoint/balances/"
    val requestToken = RequestToken.generate(requesterDid, pfiDid)

    val request = Request.Builder()
      .url(baseUrl)
      .addHeader("Content-Type", JSON_HEADER)
      .addHeader("Authorization", "Bearer $requestToken")
      .get()
      .build()

    val response: Response = client.newCall(request).execute()
    when {
      response.isSuccessful -> {
        val responseString = response.body?.string()
        // response body is an object with a data field
        val jsonNode = jsonMapper.readTree(responseString)
        return jsonNode.get("data").elements()
          .asSequence()
          .map { Balance.parse(it.toString()) }
          .toList()
      }

      else -> throw buildResponseException(response)
    }
  }

  /**
   * Send RFQ message to the PFI.
   *
   * @param rfq The RFQ to send
   *
   * @throws TbdexResponseException for response errors.
   */
  fun createExchange(rfq: Rfq) {
    validateMessage(rfq)

    val pfiDid = rfq.metadata.to
    val body: RequestBody = Json.stringify(CreateExchangeRequest(rfq))
      .toRequestBody(jsonMediaType)

    this.createExchange(pfiDid, body)
  }

  /**
   * Send RFQ message and include a replyTo URL for the PFI to send a callback to.
   *
   * @param rfq The RFQ to send
   * @param replyTo The callback URL for PFI to send messages to.
   *
   * @throws TbdexResponseException for response errors.
   */
  fun createExchange(rfq: Rfq, replyTo: String) {
    validateMessage(rfq)

    val pfiDid = rfq.metadata.to
    val body: RequestBody = Json.stringify(CreateExchangeRequest(rfq, replyTo))
      .toRequestBody(jsonMediaType)

    this.createExchange(pfiDid, body)
  }

  private fun createExchange(pfiDid: String, requestBody: RequestBody) {
    val path = "/exchanges"

    val pfiServiceEndpoint = getPfiServiceEndpoint(pfiDid)
    val url = pfiServiceEndpoint + path

    val request = Request.Builder()
      .url(url)
      .addHeader("Content-Type", JSON_HEADER)
      .post(requestBody)
      .build()

    println("Attempting to send rfq message to: ${request.url}")

    executeRequest(request)
  }

  /**
   * Send Order message to the PFI.
   *
   * @param order The Order to send
   *
   * @throws TbdexResponseException for response errors.
   */
  fun submitOrder(order: Order) {
    validateMessage(order)

    val pfiDid = order.metadata.to
    val exchangeId = order.metadata.exchangeId

    val body: RequestBody = Json.stringify(order)
      .toRequestBody(jsonMediaType)

    this.submitMessage(pfiDid, exchangeId, body)
  }

  /**
   * Send Close message to the PFI.
   *
   * @param close The Close to send
   *
   * @throws TbdexResponseException for response errors.
   */
  fun submitClose(close: Close) {
    validateMessage(close)

    val pfiDid = close.metadata.to
    val exchangeId = close.metadata.exchangeId

    val body: RequestBody = Json.stringify(close)
      .toRequestBody(jsonMediaType)

    this.submitMessage(pfiDid, exchangeId, body)
  }

  private fun submitMessage(pfiDid: String, exchangeId: String, requestBody: RequestBody) {
    val path = "/exchanges/$exchangeId"

    val pfiServiceEndpoint = getPfiServiceEndpoint(pfiDid)
    val url = pfiServiceEndpoint + path

    val request = Request.Builder()
      .url(url)
      .addHeader("Content-Type", JSON_HEADER)
      .put(requestBody)
      .build()

    println("Attempting to send message to exchange $exchangeId to: ${request.url}")

    executeRequest(request)
  }

  /**
   * Fetches a specific exchange identified by its ID from the PFI.
   *
   * @param pfiDid The decentralized identifier of the PFI.
   * @param requesterDid The decentralized identifier of the entity requesting the exchange.
   * @param exchangeId The unique identifier of the exchange to be fetched.
   * @return An [Exchange] containing the requested exchange.
   * @throws TbdexResponseException for request or response errors.
   */
  fun getExchange(pfiDid: String, requesterDid: BearerDid, exchangeId: String): Exchange {
    val pfiServiceEndpoint = getPfiServiceEndpoint(pfiDid)
    val baseUrl = "$pfiServiceEndpoint/exchanges/$exchangeId"
    val requestToken = RequestToken.generate(requesterDid, pfiDid)

    val request = Request.Builder()
      .url(baseUrl)
      .addHeader("Content-Type", JSON_HEADER)
      .addHeader("Authorization", "Bearer $requestToken")
      .get()
      .build()

    val response: Response = client.newCall(request).execute()
    println("attempting to get exchange: ${request.url}")

    when {
      response.isSuccessful -> {
        val responseString = response.body?.string()
        val jsonNode = jsonMapper.readTree(responseString)
        return jsonNode.get("data").elements().asSequence()
          .map { Message.parse(it.toString()) }
          .toList()
      }

      else -> throw buildResponseException(response)
    }
  }

  /**
   * Fetches exchanges from the PFI based on the provided filters.
   *
   * @param pfiDid The decentralized identifier of the PFI.
   * @param requesterDid The decentralized identifier of the entity requesting the exchange.
   * @param filter An optional filter to apply for fetching exchanges. If null, all exchanges for the given PFI and DID
   * will be fetched.
   * @return A list of matching [Exchange].
   * @throws TbdexResponseException for request or response errors.
   */
  fun getExchanges(pfiDid: String, requesterDid: BearerDid, filter: GetExchangesFilter? = null): List<Exchange> {
    val pfiServiceEndpoint = getPfiServiceEndpoint(pfiDid)
    val baseUrl = "$pfiServiceEndpoint/exchanges/"
    val requestToken = RequestToken.generate(requesterDid, pfiDid)

    // compose query param
    val httpUrlBuilder = baseUrl.toHttpUrl().newBuilder()
    filter?.exchangeIds?.forEach { httpUrlBuilder.addQueryParameter("id", it) }

    val request = Request.Builder()
      .url(httpUrlBuilder.build())
      .addHeader("Content-Type", JSON_HEADER)
      .addHeader("Authorization", "Bearer $requestToken")
      .get()
      .build()

    println("attempting to get exchanges: ${request.url}")

    val response: Response = client.newCall(request).execute()
    when {
      response.isSuccessful -> {
        val jsonNode = jsonMapper.readTree(response.body?.string())
        val exchanges = mutableListOf<List<Message>>()

        jsonNode.get("data").elements().forEach { jsonExchange ->
          val exchange = jsonExchange.elements().asSequence()
            .map { Message.parse(it.toString()) }
            .toList()
          exchanges.add(exchange)
        }
        return exchanges
      }

      else -> throw buildResponseException(response)
    }
  }

  /**
   * Builds an error response based on the provided HTTP response.
   *
   * @param response The HTTP response received from the PFI.
   * @return An [TbdexResponseException] containing the errors and related information extracted from the HTTP response.
   */
  private fun buildResponseException(response: Response): TbdexResponseException {
    val errors = when (val responseBody = response.body?.string()) {
      null -> listOf()
      else -> {
        val jsonNode = jsonMapper.readTree(responseBody)
        val errors = jsonNode.get("errors").elements()
          .asSequence()
          .map { jsonMapper.readValue(it.toString(), ErrorDetail::class.java) }
          .toList()
        errors
      }
    }

    return TbdexResponseException(
      "response status: ${response.code}",
      errors = errors
    )
  }

  private fun validateMessage(message: Message) {
    Validator.validateMessage(message)
    message.verify()
  }

  private fun executeRequest(request: Request) {
    val response: Response = client.newCall(request).execute()
    if (!response.isSuccessful) {
      throw buildResponseException(response)
    }
  }
}
