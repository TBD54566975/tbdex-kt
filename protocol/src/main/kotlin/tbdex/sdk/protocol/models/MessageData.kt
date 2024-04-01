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
 * This includes salted hashes of fields in RfqPrivateData.
 *
 * @property offeringId Offering which Alice would like to get a quote for
 * @property payin selected payin amount, method, and hashed details
 * @property payout selected payout method, and hashed details
 * @property claimsHash hash of claims that fulfill the requirements declared in the referenced Offering
 */
class RfqData(
  val offeringId: String,
  val payin: SelectedPayinMethod,
  val payout: SelectedPayoutMethod,
  var claimsHash: String? = null
) : MessageData

/**
 * Private data contained in a RFQ message, including data which will be placed in {@link RfqPrivateData}
 *
 * @property salt Randomly generated cryptographic salt used to hash privateData fields
 * @property payin A container for the cleartext `payin.paymentDetails`
 * @property payout A container for the cleartext `payout.paymentDetails`
 * @property claims claims that fulfill the requirements declared in an Offering
 */
class RfqPrivateData(
  val salt: String,
  val payin: PrivatePaymentDetails? = null,
  val payout: PrivatePaymentDetails? = null,
  val claims: List<String>? = null
)

/**
 * Data contained in a RFQ message, including data which will be placed in Rfq.privateDAta.
 * Used for creating an RFQ.
 *
 * @property offeringId Offering which Alice would like to get a quote for
 * @property payin selected payin amount, method, and cleartext payment details
 * @property payout selected payout method, and cleartext payment details
 * @property claims an array of hashes claims that fulfill the requirements declared in the referenced Offering
 */
class CreateRfqData(
  val offeringId: String,
  val payin: CreateSelectedPayinMethod,
  val payout: CreateSelectedPayoutMethod,
  val claims: List<String>
)

/**
 * A container for the cleartext `paymentDetails`
 *
 * @property paymentDetails An object containing the properties defined in the
 *                          respective Offering's requiredPaymentDetails json schema.
 *                          When creating an Rfq, this value is taken from
 *                          CreateSelectedPaymentMethod.paymentDetails.
 */
class PrivatePaymentDetails(
  val paymentDetails: Map<String, Any>? = null
)

/**
 * A data class representing the payment method selected.
 *
 * @property kind type of payment method
 * @property paymentDetails An object containing the properties
 *                          defined in an Offering's requiredPaymentDetails json schema
 */
sealed class SelectedPaymentMethod(
  val kind: String,
  var paymentDetailsHash: String? = null
) : MessageData

/**
 * A data class representing the payment method selected, including the cleartext payment details.
 * Used for creating an RFQ.
 * @property kind type of payment method
 * @property paymentDetails An object containing the properties
 *                          defined in an Offering's requiredPaymentDetails json schema
 */
sealed class CreateSelectedPaymentMethod(
  val kind: String,
  val paymentDetails: Map<String, Any>? = null
)

/**
 * A data class representing the payin method selected.
 *
 * @property kind type of payment method
 * @property paymentDetailsHash A hash of the object containing the properties
 *                              defined in an Offering's requiredPaymentDetails json schema
 * @property amount Amount of currency Alice wants to pay in exchange for payout currency
 */
class SelectedPayinMethod(
  kind: String,
  paymentDetailsHash: String? = null,
  val amount: String
) : SelectedPaymentMethod(kind, paymentDetailsHash)

/**
 * A data class representing the payin method selected, including the cleartext payin details.
 * @property kind type of payment method
 * @property paymentDetails An object containing the properties
 *                              defined in an Offering's requiredPaymentDetails json schema
 * @property amount Amount of currency Alice wants to pay in exchange for payout currency
 */
class CreateSelectedPayinMethod(
  kind: String,
  paymentDetails: Map<String, Any>? = null,
  val amount: String
) : CreateSelectedPaymentMethod(kind, paymentDetails)

/**
 * A data class representing the payout method selected.
 *
 * @property kind type of payment method
 * @property paymentDetailsHash A hash of the object containing the properties
 *                          defined in an Offering's requiredPaymentDetails json schema
 */
class SelectedPayoutMethod(
  kind: String,
  paymentDetailsHash: String? = null
) : SelectedPaymentMethod(kind, paymentDetailsHash)

/**
 * A data class representing the payin method selected, including the cleartext payout details.
 * @property kind type of payment method
 * @property paymentDetails An object containing the properties
 *                          defined in an Offering's requiredPaymentDetails json schema
 */
class CreateSelectedPayoutMethod(
  kind: String,
  paymentDetails: Map<String, Any>? = null,
) : CreateSelectedPaymentMethod(kind, paymentDetails)

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
 *
 * @property orderStatus Current status of Order that's being executed
 */
class OrderStatusData(val orderStatus: String) : MessageData