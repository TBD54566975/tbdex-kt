package tbdex.sdk.httpclient.models

import tbdex.sdk.protocol.models.Message

typealias Exchange = List<Message>

/**
 * Error response.
 *
 * @property errors The list of error details.
 */
class TbdexResponseException(
  message: String,
  cause: Exception? = null,
  val errors: List<ErrorDetail>?
) : Exception(message, cause)

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
