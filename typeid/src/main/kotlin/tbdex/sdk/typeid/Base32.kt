package tbdex.sdk.typeid

import kotlin.experimental.and

private val ALPHABET = "0123456789abcdefghjkmnpqrstvwxyz".toCharArray()
private val DECODE = intArrayOf(
  0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
  0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
  0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
  0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
  0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x00, 0x01,
  0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0xFF, 0xFF,
  0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E,
  0x0F, 0x10, 0x11, 0xFF, 0x12, 0x13, 0xFF, 0x14, 0x15, 0xFF,
  0x16, 0x17, 0x18, 0x19, 0x1A, 0xFF, 0x1B, 0x1C, 0x1D, 0x1E,
  0x1F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0x0A, 0x0B, 0x0C,
  0x0D, 0x0E, 0x0F, 0x10, 0x11, 0xFF, 0x12, 0x13, 0xFF, 0x14,
  0x15, 0xFF, 0x16, 0x17, 0x18, 0x19, 0x1A, 0xFF, 0x1B, 0x1C,
  0x1D, 0x1E, 0x1F, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
  0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
  0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
  0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
  0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
  0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
  0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
  0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
  0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
  0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
  0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
  0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
  0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
  0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
)

fun encode(src: ByteArray): String {
  require(src.size == 16) {
    "Invalid length"
  }

  return StringBuilder(26)
    // 10 byte timestamp
    .append(ALPHABET[((src[0] and 244.toByte()).toInt() shr 5)])
    .append(ALPHABET[(src[0] and 31).toInt()])
    .append(ALPHABET[(src[1] and 248.toByte()).toInt() shr 3])
    .append(ALPHABET[((src[1] and 7).toInt() shl 2) or ((src[2] and 192.toByte()).toInt() shr 6)])
    .append(ALPHABET[(src[2] and 62).toInt() shr 1])
    .append(ALPHABET[((src[2] and 1).toInt() shl 4) or ((src[3] and 240.toByte()).toInt() shr 4)])
    .append(ALPHABET[((src[3] and 15).toInt() shl 1) or ((src[4] and 128.toByte()).toInt() shr 7)])
    .append(ALPHABET[(src[4] and 124).toInt() shr 2])
    .append(ALPHABET[((src[4] and 3).toInt() shl 3) or ((src[5] and 224.toByte()).toInt() shr 5)])
    .append(ALPHABET[((src[5] and 31).toInt())])

    // 16 bytes of randomness
    .append(ALPHABET[(src[6] and 248.toByte()).toInt() shr 3])
    .append(ALPHABET[((src[6] and 7).toInt() shl 2) or ((src[7] and 192.toByte()).toInt() shr 6)])
    .append(ALPHABET[(src[7] and 62).toInt() shr 1])
    .append(ALPHABET[((src[7] and 1).toInt() shl 4) or ((src[8] and 240.toByte()).toInt() shr 4)])
    .append(ALPHABET[((src[8] and 15).toInt() shl 1) or ((src[9] and 128.toByte()).toInt() shr 7)])
    .append(ALPHABET[(src[9] and 124).toInt() shr 2])
    .append(ALPHABET[((src[9] and 3).toInt() shl 3) or ((src[10] and 224.toByte()).toInt() shr 5)])
    .append(ALPHABET[(src[10] and 31.toByte()).toInt()])
    .append(ALPHABET[(src[11] and 248.toByte()).toInt() shr 3])
    .append(ALPHABET[((src[11] and 7).toInt() shl 2) or ((src[12] and 192.toByte()).toInt() shr 6)])
    .append(ALPHABET[(src[12] and 62).toInt() shr 1])
    .append(ALPHABET[((src[12] and 1).toInt() shl 4) or ((src[13] and 240.toByte()).toInt() shr 4)])
    .append(ALPHABET[((src[13] and 15).toInt() shl 1) or ((src[14] and 128.toByte()).toInt() shr 7)])
    .append(ALPHABET[(src[14] and 124).toInt() shr 2])
    .append(ALPHABET[((src[14] and 3).toInt() shl 3) or ((src[15] and 224.toByte()).toInt() shr 5)])
    .append(ALPHABET[(src[15] and 31).toInt()])
    .toString()
}

fun decode(src: String): ByteArray {
  require(src.length == 26) {
    "Invalid length"
  }

  val v = src.map { it.code }

  if (DECODE[v[0]] == 0xFF
    || DECODE[v[1]] == 0xFF
    || DECODE[v[2]] == 0xFF
    || DECODE[v[3]] == 0xFF
    || DECODE[v[4]] == 0xFF
    || DECODE[v[5]] == 0xFF
    || DECODE[v[6]] == 0xFF
    || DECODE[v[7]] == 0xFF
    || DECODE[v[8]] == 0xFF
    || DECODE[v[9]] == 0xFF
    || DECODE[v[10]] == 0xFF
    || DECODE[v[11]] == 0xFF
    || DECODE[v[12]] == 0xFF
    || DECODE[v[13]] == 0xFF
    || DECODE[v[14]] == 0xFF
    || DECODE[v[15]] == 0xFF
    || DECODE[v[16]] == 0xFF
    || DECODE[v[17]] == 0xFF
    || DECODE[v[18]] == 0xFF
    || DECODE[v[19]] == 0xFF
    || DECODE[v[20]] == 0xFF
    || DECODE[v[21]] == 0xFF
    || DECODE[v[22]] == 0xFF
    || DECODE[v[23]] == 0xFF
    || DECODE[v[24]] == 0xFF
    || DECODE[v[25]] == 0xFF
  ) {
    throw IllegalArgumentException("Invalid base32 character")
  }

  return byteArrayOf(
    // 6 bytes timestamp (48 bits)
    (DECODE[src[0].code] shl 5 or DECODE[src[1].code]).toByte(),
    (DECODE[src[2].code] shl 3 or (DECODE[src[3].code] shr 2)).toByte(),
    (DECODE[src[3].code] and 3 shl 6 or (DECODE[src[4].code] shl 1) or (DECODE[src[5].code] shr 4)).toByte(),
    (DECODE[src[5].code] and 15 shl 4 or (DECODE[src[6].code] shr 1)).toByte(),
    (DECODE[src[6].code] and 1 shl 7 or (DECODE[src[7].code] shl 2) or (DECODE[src[8].code] shr 3)).toByte(),
    (DECODE[src[8].code] and 7 shl 5 or DECODE[src[9].code]).toByte(),

    // 10 bytes of entropy (80 bits)
    (DECODE[src[10].code] shl 3 or (DECODE[src[11].code] shr 2)).toByte(),
    (DECODE[src[11].code] and 3 shl 6 or (DECODE[src[12].code] shl 1) or (DECODE[src[13].code] shr 4)).toByte(),
    (DECODE[src[13].code] and 15 shl 4 or (DECODE[src[14].code] shr 1)).toByte(),
    (DECODE[src[14].code] and 1 shl 7 or (DECODE[src[15].code] shl 2) or (DECODE[src[16].code] shr 3)).toByte(),
    (DECODE[src[16].code] and 7 shl 5 or DECODE[src[17].code]).toByte(),
    (DECODE[src[18].code] shl 3 or (DECODE[src[19].code] shr 2)).toByte(),
    (DECODE[src[19].code] and 3 shl 6 or (DECODE[src[20].code] shl 1) or (DECODE[src[21].code] shr 4)).toByte(),
    (DECODE[src[21].code] and 15 shl 4 or (DECODE[src[22].code] shr 1)).toByte(),
    (DECODE[src[22].code] and 1 shl 7 or (DECODE[src[23].code] shl 2) or (DECODE[src[24].code] shr 3)).toByte(),
    (DECODE[src[24].code] and 7 shl 5 or DECODE[src[25].code]).toByte()
  )
}