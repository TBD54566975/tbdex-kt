class Playground {

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      Validator.validate(Json.objectMapper.readTree("{\"fname\": \"Alice\",  \"lname\": \"Jones\"}"), "rfq")
    }
  }

}

