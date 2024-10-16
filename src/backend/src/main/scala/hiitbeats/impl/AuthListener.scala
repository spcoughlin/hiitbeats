package hiitbeats.impl

import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}
import java.net.InetSocketAddress
import scala.concurrent.Promise


/**
 * A simple HTTP server that listens for the authorization code from the Spotify API.
 */
object AuthListener {
  def startServer(authCodePromise: Promise[String]): Unit = {
    val server = HttpServer.create(new InetSocketAddress(9000), 0)
    server.createContext("/", new AuthCodeHandler(authCodePromise, server))
    server.setExecutor(null) // Creates a default executor
    server.start()
  }

  class AuthCodeHandler(authCodePromise: Promise[String], server: HttpServer) extends HttpHandler {
    override def handle(exchange: HttpExchange): Unit = {
      val query = Option(exchange.getRequestURI.getQuery).getOrElse("")
      val params = query.split("&").flatMap { param =>
        val pair = param.split("=")
        if (pair.length == 2) Some(pair(0) -> pair(1)) else None
      }.toMap

      if (params.contains("code")) {
        val authCode = params("code")
        // Fulfill the promise
        authCodePromise.success(authCode)
        // Send a response to the browser
        val response = "Authorization code received. You can close this window."
        exchange.sendResponseHeaders(200, response.length())
        val os = exchange.getResponseBody
        os.write(response.getBytes())
        os.close()

        // Stop the server
        server.stop(0)
      } else {
        val response = "Authorization code not found."
        exchange.sendResponseHeaders(400, response.length())
        val os = exchange.getResponseBody
        os.write(response.getBytes())
        os.close()
      }
    }
  }
}

