package tbdex.sdk.protocol.models

import com.fasterxml.jackson.annotation.JsonFormat
import tbdex.sdk.protocol.serialization.dateTimeFormat
import java.time.OffsetDateTime

/**
 * An interface that represents the contents of a [Message].
 */
sealed interface MessageData : Data

/**
 * A data class implementing [MessageData] that represents the contents of an [Rfq].
 */
class RfqData(
  val offeringId: String,
  val payinAmount: String,
  val payinMethod: SelectedPaymentMethod,
  val payoutMethod: SelectedPaymentMethod,
  val claims: List<String>
) : MessageData

/**
 * A data class representing the payment method selected.
 */
class SelectedPaymentMethod(
  val kind: String,
  val paymentDetails: Map<String, Any>? = null
) : MessageData

/**
 * A data class implementing [MessageData] that represents the contents of a [Quote].
 */
class QuoteData(
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimeFormat, timezone = "UTC")
  val expiresAt: OffsetDateTime,
  val payin: QuoteDetails,
  val payout: QuoteDetails,
) : MessageData

/**
 * A data class representing details for payin and payout amounts.
 */
class QuoteDetails(
  val currencyCode: String,
  val amount: String,
  val fee: String? = null,
  val paymentInstruction: PaymentInstruction? = null
)

/**
 * A data class representing each payment instruction.
 */
class PaymentInstruction(
  val link: String?,
  val instruction: String?
)

/**
 * A data class implementing [MessageData] that represents the contents of a [Close].
 */
class CloseData(
  val reason: String? = null,
  val success: Boolean? = null
) : MessageData

/**
 * A data class implementing [MessageData] that represents the contents of an [Order].
 */
class OrderData : MessageData

/**
 * A data class implementing [MessageData] that represents the contents of an [OrderStatus].
 */
class OrderStatusData(val orderStatus: String) : MessageData