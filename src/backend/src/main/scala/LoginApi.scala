package HiitBeats

import java.util.Base64
import play.api.libs.json._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.io.Source
import scala.language.postfixOps
import scala.util.{Try, Success, Failure}
import sttp.client4._
import sttp.client4.httpclient.HttpClientSyncBackend
import sttp.model.Uri


object LoginApi {

  val file = "/Users/seancoughlin/projects/hiitbeats/src/backend/api_creds.txt"
  val lines = Source.fromFile(file).getLines().toArray
  val clientId = lines(0)
  val clientSecret = lines(1)
  val redirectUri = lines(2)
  Source.fromFile(file).close()

  // Returns the login link for the user to log in
  def getLoginLink: String = {
    val scopes = "user-read-private user-read-email playlist-modify-private playlist-modify-public"
    val state = "no-crossrefs"
    s"https://accounts.spotify.com/authorize?client_id=$clientId&response_type=code&redirect_uri=$redirectUri&scope=$scopes&state=$state"
  }

 
  def getAuthCode(loginLink: String): String = {
    println(s"Please click on the following link to authorize the application:\n$loginLink")
    val authCodePromise = Promise[String]()

    // Start the HTTP server
    AuthListener.startServer(authCodePromise)

    // Wait for the auth code to be fulfilled
    val authCode = Await.result(authCodePromise.future, 5.minutes)

    // Return the auth code
    authCode
  }

  def exchangeAuthCodeForToken(authCode: String): String = {
    val url = uri"https://accounts.spotify.com/api/token"
    val params = Map(
      "grant_type"   -> "authorization_code",
      "code"         -> authCode,
      "redirect_uri" -> redirectUri
    )
    val authString = s"$clientId:$clientSecret"
    val authHeader = "Basic " + Base64.getEncoder.encodeToString(authString.getBytes("UTF-8"))
    val backend = HttpClientSyncBackend()
    val request = basicRequest
      .post(url)
      .header("Authorization", authHeader)
      .body(params)
      .response(asStringAlways)
    val response = request.send(backend)

    if (response.code.isSuccess) {
      val json = Json.parse(response.body)
      val accessToken = (json \ "access_token").as[String]
      accessToken
    } else {
      throw new Exception(s"Failed to exchange auth code for token: ${response.body}")
    }
  }

  def getUserID(accessToken: String): String = {
    val url = uri"https://api.spotify.com/v1/me"
    val header = Map("Authorization" -> s"Bearer $accessToken")
    val backend = HttpClientSyncBackend()
    val request = basicRequest
      .get(url)
      .headers(header)
      .response(asStringAlways)
    val response = request.send(backend)

    if (response.code.isSuccess) {
      val json = Json.parse(response.body)
      val userID = (json \ "id").as[String]
      userID
    } else {
      throw new Exception(s"Failed to get user ID: ${response.body}")
    }
  }
}

