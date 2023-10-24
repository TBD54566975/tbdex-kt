package tbdex.sdk.httpserver

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.uri
import io.ktor.server.response.ApplicationResponse
import io.ktor.server.response.header
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

/**
 * Main entrypoint of the executable that starts a Netty webserver at port 8080
 * and registers the [module].
 *
 */
fun main() {
  embeddedServer(Netty, port = 8080) { module() }.start(wait = true)
}

/**
 * Defines endpoints exposed by Netty webserver.
 */
fun Application.module() {
  routing {
    get("/") {
      call.respondText { "hello world" }
    }
    post("/manual") {
      // AF4GH is a sample code for demo purposes
      call.response.header("Location", "/manual/AF4GH")
      call.response.status(HttpStatusCode.Created)
      call.respondText("Manually setting the location header")
    }
    post("/extension") {
      // AF4GH is a sample code for demo purposes
      call.response.created("AF4GH")
      call.respondText("Extension setting the location header")
    }
  }
}

private fun ApplicationResponse.created(id: String) {
  call.response.status(HttpStatusCode.Created)
  call.response.header("Location", "${call.request.uri}/$id")
}