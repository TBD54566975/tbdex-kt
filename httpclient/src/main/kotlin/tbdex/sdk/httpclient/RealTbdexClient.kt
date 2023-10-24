package tbdex.sdk.httpclient

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import tbdex.sdk.httpclient.models.ErrorDetail
import tbdex.sdk.httpclient.models.ErrorResponse
import tbdex.sdk.httpclient.models.GetExchangeResponse
import tbdex.sdk.httpclient.models.GetExchangesFilter
import tbdex.sdk.httpclient.models.GetExchangesResponse
import tbdex.sdk.httpclient.models.GetOfferingsFilter
import tbdex.sdk.httpclient.models.GetOfferingsResponse
import tbdex.sdk.httpclient.models.SendMessageResponse
import tbdex.sdk.httpclient.models.TbdexResponse
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Resource
import tbdex.sdk.protocol.serialization.Json
import web5.sdk.dids.Did

/**
 * A real client implementation of the [TbdexClient] interface for communicating with a TBD Exchange server.
 */
object RealTbdexClient : TbdexClient {
  private val client = OkHttpClient()
  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
  private const val JSON_HEADER = "application/json"

  /**
   * Fetches offerings from the TBD Exchange server.
   *
   * @param pfiDid The decentralized identifier of the PFI.
   * @param filter An optional filter to apply for fetching offerings.
   * If null, all offerings for the given PFI will be fetched.
   * @return A [TbdexResponse] containing the offerings fetched and related information, or an error response.
   */
  override fun getOfferings(pfiDid: String, filter: GetOfferingsFilter?): TbdexResponse {
    val pfiServiceEndpoint = getPfiServiceEndpoint(pfiDid)
    val baseUrl = "$pfiServiceEndpoint/offerings/"

    // compose query param
    val queryMap: Map<String, String?>? = filter?.let { Json.jsonMapper.convertValue(it) }
    val notNullQueryMap = queryMap?.filterValues { it != null }
    val httpUrlBuilder = baseUrl.toHttpUrl().newBuilder()
    notNullQueryMap?.forEach { httpUrlBuilder.addQueryParameter(it.key, it.value) }

    val request = Request.Builder()
      .url(httpUrlBuilder.build())
      .get()
      .build()

    val response: Response = client.newCall(request).execute()
    return when {
      response.isSuccessful -> {
        val responseString = response.body?.string()
        // response body is an object with a data field
        val jsonNode = Json.jsonMapper.readTree(responseString)
        val data = jsonNode.get("data").elements().asSequence()
          .map{ Resource.parse(it.toString()) }
          .toList()

        GetOfferingsResponse(
          status = response.code,
          headers = response.headers,
          data = data
        )
      }

      else -> {
        buildErrorResponse(response)
      }
    }
  }

  /**
   * Sends a message to the TBD Exchange server.
   *
   * @param message The [Message] object containing the message details to be sent.
   * @return A [TbdexResponse] indicating the success or failure of the message sending operation.
   */
  override fun sendMessage(message: Message): TbdexResponse {
    val pfiDid = message.metadata.to
    val exchangeId = message.metadata.exchangeId
    val kind = message.metadata.kind

    val pfiServiceEndpoint = getPfiServiceEndpoint(pfiDid)
    val url = "$pfiServiceEndpoint/exchanges/$exchangeId/$kind"

    val body = Json.stringify(message).toRequestBody(jsonMediaType)

    val request = Request.Builder()
      .url(url)
      .addHeader("Content-Type", JSON_HEADER)
      .post(body)
      .build()

    val response: Response = client.newCall(request).execute()
    return when {
      response.isSuccessful -> {
        SendMessageResponse(
          status = response.code,
          headers = response.headers,
        )
      }

      else -> {
        buildErrorResponse(response)
      }
    }
  }

  /**
   * Fetches a specific exchange identified by its ID from the TBD Exchange server.
   *
   * @param pfiDid The decentralized identifier of the PFI.
   * @param exchangeId The unique identifier of the exchange to be fetched.
   * @param did The decentralized identifier of the entity requesting the exchange.
   * @return A [TbdexResponse] containing the requested exchange and related information, or an error response.
   */
  override fun getExchange(pfiDid: String, exchangeId: String, did: Did): TbdexResponse {
    val pfiServiceEndpoint = getPfiServiceEndpoint(pfiDid)
    val baseUrl = "$pfiServiceEndpoint/exchanges/${exchangeId}"
    val requestToken = generateRequestToken(did)

    val request = Request.Builder()
      .url(baseUrl)
      .addHeader("Content-Type", JSON_HEADER)
      .addHeader("Authorization", "Bearer $requestToken")
      .get()
      .build()

    val response: Response = client.newCall(request).execute()
    return when {
      response.isSuccessful -> {
        val responseString = response.body?.string()
        val jsonNode = Json.jsonMapper.readTree(responseString)
        val exchange = jsonNode.get("data").elements().asSequence()
          .map { Message.parse(it.toString()) }
          .toList()

        GetExchangeResponse(
          status = response.code,
          headers = response.headers,
          data = exchange
        )
      }

      else -> {
        buildErrorResponse(response)
      }
    }
  }

  /**
   * Fetches exchanges from the TBD Exchange server based on the provided filters.
   *
   * @param pfiDid The decentralized identifier of the PFI.
   * @param did The decentralized identifier of the entity requesting the exchanges.
   * @param filter An optional filter to apply for fetching exchanges.
   * If null, all exchanges for the given PFI and DID will be fetched.
   * @return A [TbdexResponse] containing the exchanges fetched and related information, or an error response.
   */
  override fun getExchanges(
    pfiDid: String,
    did: Did,
    filter: GetExchangesFilter?
  ): TbdexResponse {
    val pfiServiceEndpoint = getPfiServiceEndpoint(pfiDid)
    val baseUrl = "$pfiServiceEndpoint/exchanges/"
    val requestToken = generateRequestToken(did)

    // compose query param
    val httpUrlBuilder = baseUrl.toHttpUrl().newBuilder()
    filter?.exchangeIds?.forEach { httpUrlBuilder.addQueryParameter("id", it) }

    val request = Request.Builder()
      .url(httpUrlBuilder.build())
      .addHeader("Content-Type", JSON_HEADER)
      .addHeader("Authorization", "Bearer $requestToken")
      .get()
      .build()

    val response: Response = client.newCall(request).execute()
    return when {
      response.isSuccessful -> {
        val responseString = response.body?.string()
        val jsonNode = Json.jsonMapper.readTree(responseString)

        val exchanges = mutableListOf<List<Message>>()
        jsonNode.get("data").elements().forEach { jsonExchange ->
          val exchange = jsonExchange.elements().asSequence()
            .map { Message.parse(it.toString()) }
            .toList()
          exchanges.add(exchange)
        }

        GetExchangesResponse(
          status = response.code,
          headers = response.headers,
          data = exchanges
        )
      }

      else -> {
        buildErrorResponse(response)
      }
    }
  }

  /**
   * Builds an error response based on the provided HTTP response.
   *
   * @param response The HTTP response received from the TBD Exchange server.
   * @return An [ErrorResponse] containing the errors and related information extracted from the HTTP response.
   */
  private fun buildErrorResponse(response: Response): ErrorResponse {

    val jsonNode = Json.jsonMapper.readTree(response.body!!.string())
    val errors = jsonNode.get("errors").elements().asSequence()
      .map { Json.jsonMapper.readValue<ErrorDetail>(it.toString()) }
      .toList()

    return ErrorResponse(
      status = response.code,
      headers = response.headers,
      errors = errors
    )
  }
}