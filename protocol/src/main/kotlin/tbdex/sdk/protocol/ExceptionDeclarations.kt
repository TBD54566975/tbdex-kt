package tbdex.sdk.protocol

/**
 * InvalidNextMessageException
 *
 * @param message the exception message detailing the error
 */
class InvalidNextMessageException(message: String? = null)
  : RuntimeException(message)

/**
 * ExchangeIdNotFoundException
 *
 * @param message the exception message detailing the error
 */
class ExchangeIdNotFoundException(message: String? = null)
  : RuntimeException(message)

/**
 * UnknownMessageKindException
 *
 * @param message the exception message detailing the error
 */
class UnknownMessageKindException(message: String? = null)
  : RuntimeException(message)
