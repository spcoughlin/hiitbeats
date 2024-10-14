package HiitBeats

import cats.effect._
import com.comcast.ip4s._
import org.http4s.dsl.io._
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.server.Server
import org.http4s.ember.server.EmberServerBuilder
import scala.concurrent.Promise
import scala.concurrent.ExecutionContext.global

object AuthListener extends IOApp {

  // A promise that will hold the authorization code once it's captured
  private val authCodePromise = Promise[String]()

  // Function to return the authorization code (as a Future/IO)
  def captureAuthCode(): IO[String] = {
    IO.fromFuture(IO(authCodePromise.future))
  }

  // Route to handle Spotify's redirect and capture the authorization code
  val spotifyRoutes = HttpRoutes.of[IO] {
    case GET -> Root / "callback" :? CodeQueryParamMatcher(authCode) =>
      // Capture the auth code by completing the promise
      IO(authCodePromise.success(authCode)) *> Ok(s"Authorization code received: $authCode")
  }.orNotFound

  // Run the server using Ember on port 9000
  def run(args: List[String]): IO[ExitCode] = {
    createEmberServer.use { server =>
      for {
        _ <- IO(println(s"Server started on ${server.address}"))
        exitCode <- IO.never.as(ExitCode.Success) // Keeps the server running
      } yield exitCode
    }
  }

  // Create the Ember server
  def createEmberServer: Resource[IO, Server] = {
    EmberServerBuilder
      .default[IO]
      .withHost(Host.fromString("localhost").get)
      .withPort(Port.fromInt(9000).get)
      .withHttpApp(spotifyRoutes)
      .build
  }

  // Query param matcher to extract the authorization code from the URL
  object CodeQueryParamMatcher extends QueryParamDecoderMatcher[String]("code")
}

