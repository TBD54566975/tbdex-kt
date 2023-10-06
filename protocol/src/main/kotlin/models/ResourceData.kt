package models

import com.networknt.schema.JsonSchema

sealed interface ResourceData

class OfferingData(
  val description: String,
  val payoutUnitsPerPayinUnit: Int,
  val payoutCurrency: CurrencyDetails,
  val payinCurrency: CurrencyDetails,
  val payinMethods: List<PaymentMethod>,
  val payoutMethods: List<PaymentMethod>,
  val requiredClaims: PresentationExchange
) : ResourceData

class CurrencyDetails(
  val currencyCode: String,
  val minSubunits: Int? = null,
  val maxSubunits: Int? = null
)

class PaymentMethod(
  val kind: String,
  val requiredPaymentDetails: JsonSchema
)

class PresentationExchange