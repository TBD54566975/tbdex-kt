class Playground {

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      Validator.validate("{\n  \"fdsafdsa\": \"Alice\",\n  \"gdfhgre\": \"Jones\"\n}", "person")
    }
  }

}

