package tbdex.sdk.typeid

import com.github.f4b6a3.uuid.UuidCreator
import java.util.function.Predicate
import java.util.regex.Pattern

class TypeId(private val prefix: String, var suffix: String? = null) {
  private val PREFIX = Pattern.compile("^[a-z]{0,62}$").asMatchPredicate()
  private val SUFFIX: Predicate<String> =
    Pattern.compile("^[0-7][0123456789abcdefghjkmnpqrstvwxyz]{1,25}$").asMatchPredicate()

  init {
    require(PREFIX.test(prefix)) {
      "$prefix is not a valid prefix"
    }

    if (suffix == null) {
      val uuidv7 = UuidCreator.getTimeOrderedEpoch()
      suffix = encode(UuidCreator.toBytes(uuidv7))
    }


    require(SUFFIX.test(suffix!!)) {
      "$suffix is not a valid suffix"
    }
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

    private fun parseUUID(uuid: String): ByteArray {
      val arr = ByteArray(16)
      var v: Long

      // Block 1
      v = uuid.substring(0, 8).toLong(16)
      arr[0] = (v ushr 24).toByte()
      arr[1] = ((v ushr 16) and 0xFF).toByte()
      arr[2] = ((v ushr 8) and 0xFF).toByte()
      arr[3] = (v and 0xFF).toByte()

      // Block 2
      v = uuid.substring(9, 13).toLong(16)
      arr[4] = (v ushr 8).toByte()
      arr[5] = (v and 0xFF).toByte()

      // Block 3
      v = uuid.substring(14, 18).toLong(16)
      arr[6] = (v ushr 8).toByte()
      arr[7] = (v and 0xFF).toByte()

      // Block 4
      v = uuid.substring(19, 23).toLong(16)
      arr[8] = (v ushr 8).toByte()
      arr[9] = (v and 0xFF).toByte()

      // Block 5
      v = uuid.substring(24, 36).toLong(16)
      arr[10] = ((v / 0x10000000000) and 0xFF).toByte()
      arr[11] = ((v / 0x100000000) and 0xFF).toByte()
      arr[12] = ((v ushr 24) and 0xFF).toByte()
      arr[13] = ((v ushr 16) and 0xFF).toByte()
      arr[14] = ((v ushr 8) and 0xFF).toByte()
      arr[15] = (v and 0xFF).toByte()

      return arr
    }
  }
}