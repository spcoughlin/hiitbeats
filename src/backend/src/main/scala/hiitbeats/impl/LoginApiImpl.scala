package hiitbeats.impl

import hiitbeats.api.LoginApi
import java.util.Base64
import play.api.libs.json._
import scala.concurrent.{Await, Promise}
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}
import sttp.model.Uri
import sttp.client4._
import sttp.client4.UriContext

object LoginApiImpl extends LoginApi {

  // Use getCredentials from BaseApi
  private val creds = getCredentials("~/projects/hiitbeats/src/backend/api_creds.txt")
  private val clientId = creds("clientId")
  private val clientSecret = creds("clientSecret")
  private val redirectUri = creds("redirectUri")

  override def getClientToken: Try[String] = Try {
    val base64Auth = Base64.getEncoder.encodeToString(s"$clientId:$clientSecret".getBytes("UTF-8"))
    val headers = Map(
      "Authorization" -> s"Basic $base64Auth",
      "Content-Type"  -> "application/x-www-form-urlencoded"
    )
    val body = "grant_type=client_credentials"

    val response = makeRequest(uri"https://accounts.spotify.com/api/token", headers, Some(body), method = "POST").get
    val json = parseJson(response)
    (json \ "access_token").as[String]
  }

  override def getLoginLink: String = {
    val scopes = "user-read-private user-read-email playlist-modify-private playlist-modify-public playlist-read-private"
    val state = "no-crossrefs"
    s"https://accounts.spotify.com/authorize?client_id=$clientId&response_type=code&redirect_uri=$redirectUri&scope=$scopes&state=$state"
  }
 
  override def getAuthCode(loginLink: String): String = {
    println(s"Please click on the following link to authorize the application:\n$loginLink")
    val authCodePromise = Promise[String]()
    AuthListener.startServer(authCodePromise)
    val authCode = Await.result(authCodePromise.future, 5.minutes)
    authCode
  }

  override def exchangeAuthCodeForToken(authCode: String): String = {
    val url = uri"https://accounts.spotify.com/api/token"
    val params = Map(
      "grant_type"   -> "authorization_code",
      "code"         -> authCode,
      "redirect_uri" -> redirectUri
    )
    val authString = s"$clientId:$clientSecret"
    val authHeader = "Basic " + Base64.getEncoder.encodeToString(authString.getBytes("UTF-8"))
    val headers = Map(
      "Authorization" -> authHeader,
      "Content-Type"  -> "application/x-www-form-urlencoded"
    )

    val responseTry = makeRequest(url, headers, Some(params), method = "POST")

    responseTry match {
      case Success(response) =>
        val json = parseJson(response)
        val accessToken = (json \ "access_token").as[String]
        accessToken
      case Failure(exception) =>
        throw new Exception(s"Failed to exchange auth code for token: ${exception.getMessage}")
    }
  }

  override def getUserID(accessToken: String): String = {
    val url = uri"https://api.spotify.com/v1/me"
    val headers = Map("Authorization" -> s"Bearer $accessToken")

    val responseTry = makeRequest(url, headers, method = "GET")

    responseTry match {
      case Success(response) =>
        val json = parseJson(response)
        val userID = (json \ "id").as[String]
        userID
      case Failure(exception) =>
        throw new Exception(s"Failed to get user ID: ${exception.getMessage}")
    }
  }
}

