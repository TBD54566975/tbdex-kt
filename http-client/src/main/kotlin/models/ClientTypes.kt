package models

import okhttp3.Headers

sealed interface Result
sealed interface ResponseBody

class HttpResponse(
  val status: Int,
  val headers: Headers,
  val body: ResponseBody? = null
) : Result

class GetOfferingsBody(
  val offerings: List<Offering>
) : ResponseBody

class GetExchangeBody(
  val exchange: List<Message>
) : ResponseBody

class GetExchangesBody(
  val exchanges: List<List<Message>>
) : ResponseBody

class ErrorResponse(
  val status: Int,
  val headers: Headers,
  val errors: List<ErrorDetail>?
) : Result

class ErrorDetail(
  val id: String?,
  val status: String?,
  val code: String?,
  val title: String?,
  val detail: String,
  val source: Source?,
  val meta: Map<String, Any?>?
)

class Source(
  val pointer: String?,
  val parameter: String?,
  val header: String?
)