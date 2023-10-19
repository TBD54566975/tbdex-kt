import okhttp3.Headers
import tbdex.sdk.protocol.models.Message
import tbdex.sdk.protocol.models.Offering

/**
 * Tbdex response parent class.
 *
 * @property status The HTTP status code of the response.
 * @property headers The HTTP headers of the response.
 */
sealed class TbdexResponse {
  abstract val status: Int
  abstract val headers: Headers
}

/**
 * Get offerings response.
 *
 * @property status The HTTP status code of the response.
 * @property headers The HTTP headers of the response.
 * @property data The list of offerings.
 */
class GetOfferingsResponse(
  override val status: Int,
  override val headers: Headers,
  val data: List<Offering>
) : TbdexResponse()

/**
 * Send message response.
 *
 * @property status The HTTP status code of the response.
 * @property headers The HTTP headers of the response.
 */
class SendMessageResponse(
  override val status: Int,
  override val headers: Headers,
) : TbdexResponse()

/**
 * Get exchange response.
 *
 * @property status The HTTP status code of the response.
 * @property headers The HTTP headers of the response.
 * @property data The list of messages.
 */
class GetExchangeResponse(
  override val status: Int,
  override val headers: Headers,
  val data: List<Message>
) : TbdexResponse()

/**
 * Get exchanges response.
 *
 * @property status The HTTP status code of the response.
 * @property headers The HTTP headers of the response.
 * @property data The list of lists of messages.
 */
class GetExchangesResponse(
  override val status: Int,
  override val headers: Headers,
  val data: List<List<Message>>
) : TbdexResponse()

/**
 * Error response.
 *
 * @property status The HTTP status code of the response.
 * @property headers The HTTP headers of the response.
 * @property errors The list of error details.
 */
class ErrorResponse(
  override val status: Int,
  override val headers: Headers,
  val errors: List<ErrorDetail>?
) : TbdexResponse()

/**
 * Error detail.
 *
 * @property id The error ID.
 * @property status The error status.
 * @property code The error code.
 * @property title The error title.
 * @property detail The error detail message.
 * @property source The error source information.
 * @property meta Additional error metadata.
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
 * Source.
 *
 * @property pointer The source pointer.
 * @property parameter The source parameter.
 * @property header The source header.
 */
class Source(
  val pointer: String?,
  val parameter: String?,
  val header: String?
)
