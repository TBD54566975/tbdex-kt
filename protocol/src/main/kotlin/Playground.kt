import Json.objectMapper
import models.Close
import models.CloseData
import models.ResourceKind
import models.Rfq
import models.RfqData
import org.json.JSONObject
import typeid.TypeID

class Playground {

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
//      Validator.validate(Json.objectMapper.readTree("{\"fname\": \"Alice\",  \"lname\": \"Jones\"}"), "rfq")

      val close = Close.create("did:web:123", "did:key:345", TypeID("exchange"), CloseData("sike"))
      val closeJsonObject = JSONObject(Json.stringify(close))
      val closeDataJsonObject = JSONObject(Json.stringify(close.data))

      VValidator.validate(closeJsonObject, "message")
      VValidator.validate(closeDataJsonObject, "close")


    }
  }

}

