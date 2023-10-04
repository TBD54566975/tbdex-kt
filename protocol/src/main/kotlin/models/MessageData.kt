package models

import com.fasterxml.jackson.annotation.JsonFormat
import dateTimeFormat
import typeid.TypeID
import java.time.OffsetDateTime

sealed interface MessageData

class RfqData(
  val offeringID: TypeID,
  val payinSubunits: Int,
  val payinMethod: SelectedPaymentMethod,
  val payoutMethod: SelectedPaymentMethod,
  val claims: List<String>
) : MessageData

class SelectedPaymentMethod(
  val kind: String,
  val paymentDetails: Map<String, Any>
) : MessageData

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
  val feeSubunits: Int?
)

class PaymentInstructions(
  val payin: PaymentInstruction?,
  val payout: PaymentInstruction?
)

class PaymentInstruction(
  val link: String?,
  val instruction: String?
)

class CloseData(val reason: String) : MessageData
class OrderData : MessageData
class OrderStatusData(val status: String) : MessageData