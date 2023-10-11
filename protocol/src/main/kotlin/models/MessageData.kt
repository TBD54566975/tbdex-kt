package models

import StringToTypeIdDeserializer
import TypeIDToStringSerializer
import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import dateTimeFormat
import typeid.TypeID
import java.time.OffsetDateTime

sealed interface MessageData

class RfqData(
  @JsonSerialize(using = TypeIDToStringSerializer::class)
  @JsonDeserialize(using = StringToTypeIdDeserializer::class)
  val offeringId: TypeID,
  val payinSubunits: Int,
  val payinMethod: SelectedPaymentMethod,
  val payoutMethod: SelectedPaymentMethod,
  val claims: List<String>
) : MessageData

class SelectedPaymentMethod(
  val kind: String,
  val paymentDetails: Map<String, Any>? = null
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

class CloseData(val reason: String) : MessageData
class OrderData : MessageData
class OrderStatusData(val status: String) : MessageData