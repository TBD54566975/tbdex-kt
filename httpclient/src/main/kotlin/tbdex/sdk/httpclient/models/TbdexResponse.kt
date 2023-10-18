package tbdex.sdk.httpclient.models

import okhttp3.Headers
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Offering

/**
 * Tbdex response parent class.
 *
 * @constructor Create empty Tbdex response
 */
sealed class TbdexResponse {
  abstract val status: Int
  abstract val headers: Headers
}

/**
 * Get offerings response
 *
 * @property status
 * @property headers
 * @property data
 * @constructor Create empty Get offerings response
 */
class GetOfferingsResponse(
  override val status: Int,
  override val headers: Headers,
  val data: List<Offering>
) : TbdexResponse()

/**
 * Send message response
 *
 * @property status
 * @property headers
 * @constructor Create empty Send message response
 */
class SendMessageResponse(
  override val status: Int,
  override val headers: Headers,
) : TbdexResponse()

/**
 * Get exchange response
 *
 * @property status
 * @property headers
 * @property data
 * @constructor Create empty Get exchange response
 */
class GetExchangeResponse(
  override val status: Int,
  override val headers: Headers,
  val data: List<Message>
) : TbdexResponse()

/**
 * Get exchanges response
 *
 * @property status
 * @property headers
 * @property data
 * @constructor Create empty Get exchanges response
 */
class GetExchangesResponse(
  override val status: Int,
  override val headers: Headers,
  val data: List<List<Message>>
) : TbdexResponse()

/**
 * Error response
 *
 * @property status
 * @property headers
 * @property errors
 * @constructor Create empty Error response
 */
class ErrorResponse(
  override val status: Int,
  override val headers: Headers,
  val errors: List<ErrorDetail>?
) : TbdexResponse()

/**
 * Error detail
 *
 * @property id
 * @property status
 * @property code
 * @property title
 * @property detail
 * @property source
 * @property meta
 * @constructor Create empty Error detail
 */
class ErrorDetail(
  val id: String?,
  val status: String?,
  val code: String?,
  val title: String?,
  val detail: String,
  val source: Source?,
  val meta: Map<String, Any?>?
)

/**
 * Source
 *
 * @property pointer
 * @property parameter
 * @property header
 * @constructor Create empty Source
 */
class Source(
  val pointer: String?,
  val parameter: String?,
  val header: String?
)
