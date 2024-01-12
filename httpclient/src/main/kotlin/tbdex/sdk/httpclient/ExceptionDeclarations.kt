package tbdex.sdk.httpclient

class RequestTokenVerificationException(cause: Throwable, message: String? = null)
  : RuntimeException(message, cause)

class RequestTokenAudMismatchException(message: String? = null)
  : RuntimeException(message)

class RequestTokenMissingClaimsException(message: String? = null)
  : RuntimeException(message)

class RequestTokenExpiredException(message: String? = null)
  : RuntimeException(message)