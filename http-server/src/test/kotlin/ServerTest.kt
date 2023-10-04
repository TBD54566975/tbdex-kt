import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import tbdex.server.*

class ServerTest {

  @Test
  fun `test manual route`() {
    withTestApplication(Application::module) {
      handleRequest(HttpMethod.Post, "/manual").apply {
        assertEquals(HttpStatusCode.Created, response.status())
        assertEquals("/manual/AF4GH", response.headers["Location"])
        assertEquals("Manually setting the location header", response.content)
      }
    }
  }

  @Test
  fun `test extension route`() {
    withTestApplication(Application::module) {
      handleRequest(HttpMethod.Post, "/extension").apply {
        assertEquals(HttpStatusCode.Created, response.status())
        assertEquals("/extension/AF4GH", response.headers["Location"])
        assertEquals("Extension setting the location header", response.content)
      }
    }
  }
}
