package models

import com.fasterxml.jackson.annotation.JsonFormat
import dateTimeFormat
import typeid.TypeID
import java.time.OffsetDateTime

/**
 * An interface that represents the contents of a [Message]
 */
sealed interface MessageData

/**
 * A data class implementing [MessageData] that represents the contents of an [Rfq]
 */
class RfqData(
  val offeringID: TypeID,
  val payinSubunits: Int,
  val payinMethod: SelectedPaymentMethod,
  val payoutMethod: SelectedPaymentMethod,
  val claims: List<String>
) : MessageData

class SelectedPaymentMethod(
  val kind: String,
  val paymentDetails: Map<String, Any>? = null
) : MessageData

/**
 * A data class implementing [MessageData] that represents the contents of a [Quote]
 */
class QuoteData(
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimeFormat, timezone = "UTC")
  val expiresAt: OffsetDateTime,
  val payin: QuoteDetails,
  val payout: QuoteDetails,
  val paymentInstructions: PaymentInstructions? = null
) : MessageData

class QuoteDetails(
  val currencyCode: String,
  val amountSubunits: Int,
  val feeSubunits: Int? = null
)

class PaymentInstructions(
  val payin: PaymentInstruction?,
  val payout: PaymentInstruction?
)

class PaymentInstruction(
  val link: String?,
  val instruction: String?
)

/**
 * A data class implementing [MessageData] that represents the contents of a [Close]
 */
class CloseData(val reason: String) : MessageData

/**
 * A data class implementing [MessageData] that represents the contents of an [Order]
 */
class OrderData : MessageData

/**
 * A data class implementing [MessageData] that represents the contents of an [OrderStatus]
 */
class OrderStatusData(val status: String) : MessageData