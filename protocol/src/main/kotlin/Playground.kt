class Playground {

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      Validator.validate("{\"fdsafdsa\": \"Alice\",  \"gdfhgre\": \"Jones\"}", "person")
    }
  }

}

