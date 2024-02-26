package tbdex.sdk.protocol

import de.fxlae.typeid.TypeId
import java.lang.IllegalStateException

fun validateExchangeId(id: String) {
  // check that exchangeId is a valid TypeId
  try {
    val typeId = TypeId.parse(id)
    check(typeId.prefix == "rfq")
  } catch (e: IllegalArgumentException) {
    throw IllegalArgumentException("exchangeId: $id is not a valid TypeId")
  } catch (e: IllegalStateException) {
    throw IllegalStateException("exchangeId: $id does not have a valid prefix")
  }
}