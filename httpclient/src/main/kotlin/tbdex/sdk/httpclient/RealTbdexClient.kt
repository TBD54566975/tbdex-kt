package tbdex.sdk.httpclient

import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import tbdex.sdk.httpclient.Json.objectMapper
import tbdex.sdk.httpclient.models.*
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Offering
import web5.sdk.dids.Did
import web5.sdk.dids.DidResolutionResult
import web5.sdk.dids.DidResolvers

object RealTbdexClient : TbdexClient {
  private val client = OkHttpClient()
  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
  private const val JSON_HEADER = "application/json"

  init {
    DidResolvers.addResolver("ion") { did, _ ->
      // TODO drop in logic here
      DidResolutionResult()
    }
  }

  override fun getOfferings(pfiDid: String, filter: GetOfferingsFilter?): TbdexResponse {
    val pfiServiceEndpoint = getPfiServiceEndpoint(pfiDid)
    val baseUrl = "$pfiServiceEndpoint/offerings/"

    // compose query param
    val queryMap = objectMapper.convertValue(filter, Map::class.java) as Map<String, String?>
    val notNullQueries = queryMap.filterValues { it != null }
    val httpUrlBuilder = baseUrl.toHttpUrl().newBuilder()
    notNullQueries.forEach { httpUrlBuilder.addQueryParameter(it.key, it.value) }

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
        val errors: List<ErrorDetail> =
          objectMapper.readerForListOf(ErrorDetail::class.java).readValue(response.body!!.string())

        ErrorResponse(
          status = response.code,
          headers = response.headers,
          errors = errors
        )
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
        val errors: List<ErrorDetail> =
          objectMapper.readerForListOf(ErrorDetail::class.java).readValue(response.body!!.string())

        ErrorResponse(
          status = response.code,
          headers = response.headers,
          errors = errors
        )
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
        val errors: List<ErrorDetail> =
          objectMapper.readerForListOf(ErrorDetail::class.java).readValue(response.body!!.string())

        ErrorResponse(
          status = response.code,
          headers = response.headers,
          errors = errors
        )
      }
    }
  }

  override fun getExchanges(
    pfiDid: String,
    did: Did,
    filter: GetExchangesFilter?
  ): TbdexResponse {
    val pfiServiceEndpoint = getPfiServiceEndpoint(pfiDid)
    val baseUrl = "$pfiServiceEndpoint/exchanges"
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
        val errors: List<ErrorDetail> =
          objectMapper.readerForListOf(ErrorDetail::class.java).readValue(response.body!!.string())

        ErrorResponse(
          status = response.code,
          headers = response.headers,
          errors = errors
        )
      }
    }
  }
}