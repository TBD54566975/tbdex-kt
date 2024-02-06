package tbdex.sdk.httpclient

/**
 * Request token verification exception
 *
 * @param cause the underlying exception
 * @param message the exception message detailing the error
 */
class RequestTokenVerificationException(cause: Throwable, message: String? = null)
  : RuntimeException(message, cause)

/**
 * Request token audience mismatch exception
 *
 * @param message the exception message detailing the error
 */
class RequestTokenAudMismatchException(message: String? = null)
  : RuntimeException(message)

/**
 * Request token missing claims exception
 *
 * @param message the exception message detailing the error
 */
class RequestTokenMissingClaimsException(message: String? = null)
  : RuntimeException(message)

/**
 * Request token expired exception
 *
 * @param message the exception message detailing the error
 */
class RequestTokenExpiredException(message: String? = null)
  : RuntimeException(message)


/**
 * Request token expired exception
 *
 * @param message the exception message detailing the error
 */
class RequestTokenCreateException(message: String? = null)
  : RuntimeException(message)