package tbdex.sdk.protocol.models

import de.fxlae.typeid.TypeId
import tbdex.sdk.protocol.Validator
import java.time.OffsetDateTime

/**
 * Balance is a protected resource used to communicate
 * the available amount of each currency held by the PFI on behalf of its customer.
 *
 * @property metadata An object containing fields about the resource
 * @property data The actual resource content. This will always be a JSON object.
 *                The Resource Kinds section specifies the content for each individual resource type
 * @property signature A message or resource signature is a detached compact JWS as defined in RFC-7515
 *
 *  ### Example Usage:
 *  ```kotlin
 *  val balance = Balance.create(from, data)
 *  ```
 */
class Balance(
  override val metadata: ResourceMetadata,
  override val data: BalanceData,
  override var signature: String? = null
) : Resource() {
  companion object {

    /**
     * Takes an existing Balance in the form of a json string and parses it into a Balance object.
     * Validates object structure and performs an integrity check using the signature.
     *
     * @param payload The Balance as a json string.
     * @return The json string parsed into a concrete Balance implementation.
     */
    fun parse(payload: String) = Resource.parse(payload) as Balance

    /**
     * Creates a new `Balance` resource, autopopulating the id, creation/updated time, and resource kind.
     *
     * @param from DID of the PFI.
     * @param data Specific parameters relevant to a Balance.
     * @param protocol version of the tbdex protocol.
     * @return Balance instance.
     */
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