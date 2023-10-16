package tbdex.sdk.httpclient

import okhttp3.Headers

interface HttpResponse {
  val status: Int
  val headers: Headers
}

data class ErrorDetail(
  val id: String? = null,
  val status: String? = null,
  val code: String? = null,
  val title: String? = null,
  val detail: String,
  val source: Source? = null,
  val meta: Map<String, Any>? = null
) {
  data class Source(
    val pointer: String? = null,
    val parameter: String? = null,
    val header: String? = null
  )
}

data class DataResponse<T>(
  override val status: Int,
  override val headers: Headers,
  val data: T,
  val errors: Nothing? = null
) : HttpResponse

data class ErrorResponse(
  override val status: Int,
  override val headers: Headers,
  val data: Nothing? = null,
  val errors: List<ErrorDetail>
) : HttpResponse
