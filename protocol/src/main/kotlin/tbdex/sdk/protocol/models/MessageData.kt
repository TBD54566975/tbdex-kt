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
 *
 * @property offeringId Offering which Alice would like to get a quote for
 * @property payin selected payin amount, method, and details
 * @property payout selected payout method, and details
 * @property claims an array of claims that fulfill the requirements declared in the referenced Offering
 */
class RfqData(
  val offeringId: String,
  val payin: SelectedPayinMethod,
  val payout: SelectedPayoutMethod,
  val claims: List<String>
) : MessageData

/**
 * A data class representing the payment method selected.
 *
 * @property kind type of payment method
 * @property paymentDetails An object containing the properties
 *                          defined in an Offering's requiredPaymentDetails json schema
 */
sealed class SelectedPaymentMethod(
  val kind: String,
  val paymentDetails: Map<String, Any>? = null
) : MessageData

/**
 * A data class representing the payin method selected.
 *
 * @property kind type of payment method
 * @property paymentDetails An object containing the properties
 *                          defined in an Offering's requiredPaymentDetails json schema
 * @property amount Amount of currency Alice wants to pay in exchange for payout currency
 */
class SelectedPayinMethod(
  kind: String,
  paymentDetails: Map<String, Any>? = null,
  val amount: String
) : SelectedPaymentMethod(kind, paymentDetails)


/**
 * A data class representing the payout method selected.
 *
 * @property kind type of payment method
 * @property paymentDetails An object containing the properties
 *                          defined in an Offering's requiredPaymentDetails json schema
 */
class SelectedPayoutMethod(
  kind: String,
  paymentDetails: Map<String, Any>? = null
) : SelectedPaymentMethod(kind, paymentDetails)

/**
 * A data class implementing [MessageData] that represents the contents of a [Quote].
 *
 * @property expiresAt When this quote expires. ISO8601 timestamp
 * @property payin the amount of payin currency that the PFI will receive
 * @property payout the amount of payout currency that Alice will receive
 */
class QuoteData(
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = dateTimeFormat, timezone = "UTC")
  val expiresAt: OffsetDateTime,
  val payin: QuoteDetails,
  val payout: QuoteDetails,
) : MessageData

/**
 * A data class representing details for payin and payout amounts.
 *
 * @property currencyCode ISO 3166 currency code string
 * @property amount The amount of currency paid to the PFI or by the PFI excluding fees
 * @property fee The amount paid in fees
 * @property paymentInstruction Object that describes how to pay the PFI
 *                              and how to get paid by the PFI (e.g. BTC address, payment link)
 */
class QuoteDetails(
  val currencyCode: String,
  val amount: String,
  val fee: String? = null,
  val paymentInstruction: PaymentInstruction? = null
)

/**
 * Payment instruction
 *
 * @property link Link to allow Alice to pay PFI, or be paid by the PFI
 * @property instruction Instruction on how Alice can pay PFI
 *                       or how Alice can be paid by the PFI
 */
class PaymentInstruction(
  val link: String?,
  val instruction: String?
)

/**
 * A data class implementing [MessageData] that represents the contents of a [Close].
 *
 * @property reason an explanation of why the exchange is being closed/completed
 * @property success indicates whether the exchange successfully completed
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