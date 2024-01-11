package tbdex.sdk.httpclient

class RequestTokenVerificationException(cause: Throwable, message: String? = null)
  : RuntimeException(message, cause)

class RequestTokenAudiencePfiMismatchException(message: String? = null)
  : RuntimeException(message)

class MissingRequiredClaimsException(message: String? = null)
  : RuntimeException(message)

class RequestTokenExpiredException(message: String? = null)
  : RuntimeException(message)