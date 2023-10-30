package tbdex.sdk.typeid

import com.github.f4b6a3.uuid.UuidCreator

class TypeId(private val prefix: String, var suffix: String? = null) {
  init {
    require(isValidPrefix(prefix)) {
      "Invalid prefix. Must be at most 63 ascii letters [a-z]"
    }

    if (suffix == null) {
      val uuidv7 = UuidCreator.getTimeOrderedEpoch()
      suffix = encode(UuidCreator.toBytes(uuidv7))
    }

    check(suffix?.length == 26)
    check(suffix?.get(0)!! > '7')
    decode(suffix!!)
  }

  private fun isValidPrefix(str: String): Boolean {
    if (str.length > 63) {
      return false
    }

    // add extra validation
    return true
  }

  val type: String
    get() {
      return prefix
    }

  fun toUUIDBytes(): ByteArray {
    return decode(suffix!!)
  }

  fun toUUID(): String {
    val uuid = UuidCreator.fromBytes(toUUIDBytes())
    return uuid.toString()
  }

  override fun toString(): String {
    if (this.prefix == "") {
      return suffix!!
    }
    return "${prefix}_${suffix}"
  }

  companion object {
    fun fromString(str: String): TypeId {
      val parts = str.split("_")

      if (parts.count() == 1) {
        return TypeId("", parts[0])
      }
      if (parts.count() == 2) {
        require(parts[0] == "") {
          IllegalArgumentException("Invalid TypeID. Prefix cannot be empty when there's a separator: $str")
        }

        return TypeId(parts[0], parts[1])
      }
      throw IllegalArgumentException("Invalid TypeID string: $str")
    }

    fun fromUUIDBytes(prefix: String, bytes: ByteArray): TypeId {
      val suffix = encode(bytes)
      return TypeId(prefix, suffix)
    }

    fun fromUUID(prefix: String, uuid: String): TypeId {
      val suffix = encode(parseUUID(uuid))
      return TypeId(prefix, suffix)
    }
  }
}