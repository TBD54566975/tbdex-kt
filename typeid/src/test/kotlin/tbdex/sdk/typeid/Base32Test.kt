package tbdex.sdk.typeid

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class Base32Test {
  @Test
  fun `encode and decode are inverses`() {
    val originalData: ByteArray = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)

    val encodedData = encode(originalData)
    val decodedData = decode(encodedData)

    assertThat(encodedData).isEqualTo("00041061050r3gg28a1c60t3gf")
    assertThat(decodedData).isEqualTo(originalData)
  }
}