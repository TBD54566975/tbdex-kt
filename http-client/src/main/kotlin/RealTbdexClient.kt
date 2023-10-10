import Json.objectMapper
import models.ErrorDetail
import models.ErrorResponse
import models.GetExchangeBody
import models.GetExchangeOptions
import models.GetExchangesBody
import models.GetExchangesOptions
import models.GetOfferingsBody
import models.GetOfferingsOptions
import models.HttpResponse
import models.Message
import models.Offering
import models.ResponseBody
import models.Result
import models.SendMessageOptions
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

object RealTbdexClient : TbdexClient {
  private val client = OkHttpClient()
  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
  private const val JSON_HEADER = "application/json"
  override fun getOfferings(options: GetOfferingsOptions): Result {
    val pfiDid = options.pfiDid
    val pfiServiceEndpoint = getPfiServiceEndpoint(pfiDid)
    val baseUrl = "$pfiServiceEndpoint/offerings/"

    // compose query param
    val queryMap = objectMapper.convertValue(options.filter, Map::class.java) as Map<String, String?>
    val notNullQueries = queryMap.filterValues { it != null }
    val httpUrlBuilder = baseUrl.toHttpUrl().newBuilder()
    notNullQueries.forEach { httpUrlBuilder.addQueryParameter(it.key, it.value) }

    val request = Request.Builder()
      .url(httpUrlBuilder.build())
      .get()
      .build()

    return executeRequest(request) { responseString ->
      val offerings: List<Offering> = objectMapper.readerForListOf(Offering::class.java).readValue(responseString)

      GetOfferingsBody(offerings = offerings)
    }
  }

  override fun sendMessage(options: SendMessageOptions): Result {
    val message = options.message

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


    return executeRequest(request) { _ -> null }
  }

  override fun getExchange(options: GetExchangeOptions): Result {
    val pfiDid = options.pfiDid
    val pfiServiceEndpoint = getPfiServiceEndpoint(pfiDid)
    val baseUrl = "$pfiServiceEndpoint/exchanges/${options.exchangeId}"
    val requestToken = generateRequestToken(options.did)

    val request = Request.Builder()
      .url(baseUrl)
      .addHeader("Content-Type", JSON_HEADER)
      .addHeader("Authorization", "Bearer $requestToken")
      .get()
      .build()

    return executeRequest(request) { responseString ->
      val exchange: List<Message> = objectMapper.readerForListOf(Message::class.java).readValue(responseString)

      GetExchangeBody(exchange = exchange)
    }
  }

  override fun getExchanges(options: GetExchangesOptions): Result {
    val pfiDid = options.pfiDid
    val pfiServiceEndpoint = getPfiServiceEndpoint(pfiDid)
    val baseUrl = "$pfiServiceEndpoint/exchanges"
    val requestToken = generateRequestToken(options.did)

    // compose query param
    val httpUrlBuilder = baseUrl.toHttpUrl().newBuilder()
    options.filter?.exchangeIds?.forEach { httpUrlBuilder.addQueryParameter("id", it) }

    val request = Request.Builder()
      .url(httpUrlBuilder.build())
      .addHeader("Content-Type", JSON_HEADER)
      .addHeader("Authorization", "Bearer $requestToken")
      .get()
      .build()

    return executeRequest(request) { responseString ->
      val responseTree = objectMapper.readTree(responseString)
      val exchanges = mutableListOf<List<Message>>()
      responseTree.elements().forEach {
        val exchange: List<Message> = objectMapper.readerForListOf(Message::class.java).readValue(it.textValue())
        exchanges.add(exchange)
      }

      GetExchangesBody(exchanges = exchanges)
    }
  }

  private fun executeRequest(request: Request, processResponse: (String?) -> ResponseBody?): Result {
    val response: Response = client.newCall(request).execute()
    return when {
      response.isSuccessful -> {
        val responseString = response.body?.string()

        HttpResponse(
          status = response.code,
          headers = response.headers,
          body = processResponse(responseString)
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