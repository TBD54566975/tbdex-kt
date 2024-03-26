package tbdex.sdk.protocol.models

import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.Validator
import java.time.OffsetDateTime

class Balance(
  override val metadata: ResourceMetadata,
  override val data: BalanceData,
  override var signature: String? = null
) : Resource() {
  companion object {
    fun parse(toString: String) = Resource.parse(toString) as Balance

    fun create(from: String, data: BalanceData, protocol: String = "1.0"): Balance {
      val now = OffsetDateTime.now()
      val metadata = ResourceMetadata(
        kind = ResourceKind.balance,
        from = from,
        id = TypeId.generate(ResourceKind.balance.name).toString(),
        protocol = protocol,
        createdAt = now,
        updatedAt = now
      )
      Validator.validateData(data, "balance")

      return Balance(metadata, data)
    }
  }
}