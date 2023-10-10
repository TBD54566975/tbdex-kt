import models.GetExchangeOptions
import models.GetExchangesOptions
import models.GetOfferingsOptions
import models.Result
import models.SendMessageOptions

interface TbdexClient {
  fun getOfferings(options: GetOfferingsOptions): Result
  fun sendMessage(options: SendMessageOptions): Result
  fun getExchange(options: GetExchangeOptions): Result
  fun getExchanges(options: GetExchangesOptions): Result
}
