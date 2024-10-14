package HiitBeats

import scala.util.{Try, Success, Failure}
import scala.concurrent.ExecutionContext
import cats.effect.{IO, IOApp, ExitCode}

object TestMain extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    implicit val ec: ExecutionContext = ExecutionContext.global

    // Step 1: Get the login link for the user to log in
    val loginLink = ApiUtils.getLoginLink()

    // Step 2: Log in the user, get the auth code
    val authCodeFuture = ApiUtils.getAuthCode(loginLink)

    // Step 3: Once the auth code is received, exchange it for an access token
    authCodeFuture.onComplete {
      case scala.util.Success(authCode) =>
        println(s"Authorization Code received: $authCode")

        // Exchange the auth code for an access token
        val accessToken = ApiUtils.exchangeAuthCodeForToken(authCode)
        println(s"Access Token: $accessToken")

      case scala.util.Failure(ex) =>
        println(s"Failed to get authorization code: ${ex.getMessage}")
    }
  }
}
