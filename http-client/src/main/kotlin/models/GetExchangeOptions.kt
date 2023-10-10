package models

import web5.sdk.dids.Did

class GetExchangeOptions(
  val pfiDid: String,
  val exchangeId: String,
  val did: Did
)