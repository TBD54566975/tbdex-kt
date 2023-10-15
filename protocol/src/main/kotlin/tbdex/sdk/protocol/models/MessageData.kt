package tbdex.sdk.protocol.models

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import tbdex.sdk.protocol.StringToTypeIdDeserializer
import tbdex.sdk.protocol.TypeIDToStringSerializer
import tbdex.sdk.protocol.dateTimeFormat
import typeid.TypeID
import java.time.OffsetDateTime

/**
 * An interface that represents the contents of a [Message].
 */
sealed interface MessageData

/**
 * A data class implementing [MessageData] that represents the contents of an [Rfq].
 */
class RfqData(
  @JsonSerialize(using = TypeIDToStringSerializer::class)
  @JsonDeserialize(using = StringToTypeIdDeserializer::class)
  val offeringId: TypeID,
  val payinSubunits: String,
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
  val paymentInstructions: PaymentInstructions? = null
) : MessageData

/**
 * A data class representing details for payin and payout amounts.
 */
class QuoteDetails(
  val currencyCode: String,
  val amountSubunits: String,
  val feeSubunits: String? = null
)

/**
 * A data class representing payment instructions for payin/payout.
 */
class PaymentInstructions(
  val payin: PaymentInstruction?,
  val payout: PaymentInstruction?
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
class CloseData(val reason: String) : MessageData

/**
 * A data class implementing [MessageData] that represents the contents of an [Order].
 */
class OrderData : MessageData

/**
 * A data class implementing [MessageData] that represents the contents of an [OrderStatus].
 */
class OrderStatusData(val status: String) : MessageData