package tbdex.sdk.httpclient.models

import okhttp3.Headers
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Offering

sealed class TbdexResponse {
  abstract val status: Int
  abstract val headers: Headers
}

class GetOfferingsResponse(
  override val status: Int,
  override val headers: Headers,
  val data: List<Offering>
) : TbdexResponse()

class SendMessageResponse(
  override val status: Int,
  override val headers: Headers,
) : TbdexResponse()

class GetExchangeResponse(
  override val status: Int,
  override val headers: Headers,
  val data: List<Message>
) : TbdexResponse()

class GetExchangesResponse(
  override val status: Int,
  override val headers: Headers,
  val data: List<List<Message>>
) : TbdexResponse()

class ErrorResponse(
  override val status: Int,
  override val headers: Headers,
  val errors: List<ErrorDetail>?
) : TbdexResponse()

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
