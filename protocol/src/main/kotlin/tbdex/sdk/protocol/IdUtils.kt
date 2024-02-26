package tbdex.sdk.protocol

import de.fxlae.typeid.TypeId

/**
 * Validates the given exchange ID to ensure it conforms to a specific format and prefix.
 *
 * This function checks if the provided `id` is a valid [TypeId] and whether its prefix matches the expected "rfq"
 * prefix.
 *
 * @param id The exchange ID string that needs to be validated. It is expected to follow the structure of a [TypeId].
 *
 * @throws IllegalArgumentException If the `id` cannot be parsed into a [TypeId], indicating that the `id`
 * does not conform to the expected structure or format.
 *
 * @throws IllegalStateException If the parsed [TypeId] does not have the "rfq" prefix, indicating that the `id`
 * does not meet the specific validation criteria required for exchange IDs.
 *
 * Usage example:
 * ```
 * try {
 *     validateExchangeId("rfq12345")
 * } catch (e: Exception) {
 *     println(e.message)
 * }
 * ```
 */
fun validateExchangeId(id: String) {
  try {
    val typeId = TypeId.parse(id)
    check(typeId.prefix == "rfq")
  } catch (e: IllegalArgumentException) {
    throw IllegalArgumentException("exchangeId: $id is not a valid TypeId")
  } catch (e: IllegalStateException) {
    throw IllegalStateException("exchangeId: $id does not have a valid prefix")
  }
}