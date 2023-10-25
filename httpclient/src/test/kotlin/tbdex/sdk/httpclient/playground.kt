package tbdex.sdk.httpclient

import com.nimbusds.jose.JWSAlgorithm
import web5.sdk.crypto.InMemoryKeyManager
import web5.sdk.dids.DidKey

fun main() {
  val didKey = DidKey.create(InMemoryKeyManager())
  println(didKey.uri)
  val keyAlias = didKey.keyManager.generatePrivateKey(JWSAlgorithm.ES256K)
  val wat = didKey.keyManager.getPublicKey(keyAlias)

  val didResolutionResult = DidKey.resolve(didKey.uri)
  println(didResolutionResult)
}