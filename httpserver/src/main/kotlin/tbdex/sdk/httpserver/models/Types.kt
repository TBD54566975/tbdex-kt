package tbdex.sdk.httpserver.models

import io.ktor.http.HttpStatusCode
import tbdex.sdk.httpclient.models.ErrorDetail

typealias TbdexCallback = () -> Unit

class CallbackError(val statusCode: HttpStatusCode, val details: List<ErrorDetail>?) : Exception()