class Playground {

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      Validator.validate("{\"fname\": \"Alice\",  \"lname\": \"Jones\"}", "rfq")
    }
  }

}

