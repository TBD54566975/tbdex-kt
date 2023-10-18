package tbdex.sdk.httpclient

import com.fasterxml.jackson.module.kotlin.convertValue
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import tbdex.sdk.httpclient.Json.objectMapper
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
import tbdex.sdk.protocol.models.Offering
import web5.sdk.dids.Did
import web5.sdk.dids.DidKey
import web5.sdk.dids.DidResolvers

/**
 * Real tbdex client
 *
 * @constructor Create empty Real tbdex client
 */
object RealTbdexClient : TbdexClient {
  private val client = OkHttpClient()
  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
  private const val JSON_HEADER = "application/json"

  init {
    // methodName of ion does not fit with the resolver
    DidResolvers.addResolver("ion") { did, resolverDidOptions ->
      DidKey.resolve(did, resolverDidOptions)
      // todo need this? DidIon.resolve(did, resolverDidOptions)
    }
  }

  override fun getOfferings(pfiDid: String, filter: GetOfferingsFilter?): TbdexResponse {
    val pfiServiceEndpoint = getPfiServiceEndpoint(pfiDid)
    val baseUrl = "$pfiServiceEndpoint/offerings/"

    // compose query param
    val queryMap: Map<String, String?>? = filter?.let { objectMapper.convertValue(it) }
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
        val jsonNode = objectMapper.readTree(responseString)
        val jsonData = jsonNode.get("data").asText()

        // response body is an object with a data field
        val data: List<Offering> = objectMapper.readerForListOf(Offering::class.java).readValue(jsonData)

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
        val jsonNode = objectMapper.readTree(responseString)

        val exchange = mutableListOf<Message>()
        jsonNode.get("data").elements().forEach { exchange.add(Message.parse(it.asText())) }

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
        val jsonNode = objectMapper.readTree(responseString)

        val exchanges = mutableListOf<List<Message>>()
        jsonNode.get("data").elements().forEach { jsonExchange ->
          val exchange = mutableListOf<Message>()
          jsonExchange.elements().forEach { exchange.add(Message.parse(it.asText())) }
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

  private fun buildErrorResponse(response: Response): ErrorResponse {
    val errors: List<ErrorDetail> =
      objectMapper.readerForListOf(ErrorDetail::class.java).readValue(response.body!!.string())

    return ErrorResponse(
      status = response.code,
      headers = response.headers,
      errors = errors
    )
  }
}