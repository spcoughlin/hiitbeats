package HiitBeats

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.unsafe.implicits.global
import java.util.Base64
import play.api.libs.json._
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.io.Source
import scala.util.{Try, Success, Failure}
import sttp.client4._
import sttp.client4.httpclient.HttpClientSyncBackend
import sttp.model.Uri


// Case class to store what we want from a Spotify Song
case class Song(name: String, 
                artist: String, 
                duration: Int, 
                uri: String)


object ApiUtils {

  /* Gets the authentication token from the Spotify API
   * No args, returns String
   */
  def getToken: Try[String] = Try {

    // Read the client id and secret from a file
    val file = "/Users/seancoughlin/projects/hiitbeats/src/backend/api_creds.txt"
    val lines = Source.fromFile(file).getLines.toArray
    val clientId = lines(0)
    val clientSecret = lines(1)
    Source.fromFile(file).close()
    
    // Set up the request
    val base64Auth = Base64.getEncoder.encodeToString(s"$clientId:$clientSecret".getBytes)
    val authHeader = Map("Authorization" -> s"Basic $base64Auth")
    val authBody = Map("grant_type" -> "client_credentials")
    val backend = HttpClientSyncBackend()
    
    // Send the request
    val response = basicRequest
      .post(uri"https://accounts.spotify.com/api/token")
      .headers(authHeader)
      .body(authBody)
      .send(backend)
    backend.close()

    // Parse the response
    val jsonString = response.body match {
      case Left(value) => value
      case Right(value) => value
    }
    val json = Json.parse(jsonString)

    // Extract the token from the response
    (json \ "access_token").as[String]

  }

  /* Gets the songs data from the Spotify API
   * Takes a token: String, returns List[Song]
   */
  def findSongs(token : String): List[Song] = {

    // Set up the request
    val header = Map("Authorization" -> s"Bearer $token")
    val params = Map("q" -> "kanye", "limit" -> "50", "type" -> "track")
    val uriWithParams: Uri = uri"https://api.spotify.com/v1/search"
      .params(params)
    val backend = HttpClientSyncBackend()

    // Send the request
    val response = basicRequest
      .get(uriWithParams)
      .headers(header)
      .send(backend)
    backend.close()

    // Parse the response
    val jsonString = response.body match {
      case Left(value) => value
      case Right(value) => value
    }
    val json = Json.parse(jsonString)

    // Extract the songs from the response
    val rawSongs: Option[JsArray] = (json \ "tracks" \ "items").asOpt[JsArray]

    // Helper function to convert a value to a Song
    def tuplize(e: JsValue): Song = {
      val name: Option[String] = (e \ "name").asOpt[String]
      val artist: Option[String] = (e \ "artists")(0) \ "name" match {
        case JsDefined(JsString(name)) => Some(name)
        case _ => None
      }
      val duration: Option[Int] = (e \ "duration_ms").asOpt[Int]
      val uri: Option[String] = (e \ "uri").asOpt[String]
      (name, artist, duration, uri) match {
        case (Some(name), Some(artist), Some(duration), Some(uri)) => Song(name, artist, duration, uri)
        case _ => Song("Error", "Error", 0, "Error")
      }
    }
    // map the rawSongs to a list of Songs and return
    rawSongs match {
      case Some(songs) => songs.value.toList.map(e => tuplize(e))
      case None => List(Song("Error", "Error", 0, "Error"))
    }
  }

  /* Fills a workout with intervals and rest, starting with rest period
   * Takes intLength: Int, restLength: Int, totalLength: Int
   * Returns List[Int]
   */
  def fillWorkout(intLength: Int, restLength: Int, totalLength: Int): List[Int] = {
    @tailrec
    def fillWorkoutHelper(intLength: Int, restLength: Int, totalLength: Int, acc: List[Int]): List[Int] = {
      if (totalLength <= 0) acc
      else if (totalLength - intLength <= 0) acc :+ totalLength
      else fillWorkoutHelper(intLength, restLength, totalLength - intLength - restLength, acc :+ (restLength * 60000) :+ (intLength * 60000))
    }
    fillWorkoutHelper(intLength, restLength, totalLength, List())
  }  
  
  /* Matches songs to a workout and returns the "uris" string needed to add to playlist
   * Takes workout: List[Int], songs: List[Song]
   * Returns String
   */
  def matchSongs(workout: List[Int], songs: List[Song]): String = {
    def getClosestSong(interval: Int, songs: List[Song]): Song = {
      songs.minBy(s => Math.abs(s.duration - interval))
    }
    @tailrec
    def matchSongsHelper(workout: List[Int], songs: List[Song], acc: String): String = {
      workout match {
        case Nil => acc
        case interval :: rest => 
          val closestSong = getClosestSong(interval, songs)
          println(s"Matching ${interval.toFloat/60000.00} to ${closestSong.name} by ${closestSong.artist} of length ${closestSong.duration.toFloat / 60000.00}")
          matchSongsHelper(rest, songs.filterNot(_ == closestSong), acc + closestSong.uri + ",")
      }
    }
    matchSongsHelper(workout, songs, "").stripSuffix(",")
  }

  // Returns the login link for the user to log in
  def getLoginLink(): String = {
    // read our shit from the file
    val file = "/Users/seancoughlin/projects/hiitbeats/src/backend/api_creds.txt"
    val lines = Source.fromFile(file).getLines.toArray
    val clientId = lines(0)
    val clientSecret = lines(1)
    val redirectUri = lines(2)
    Source.fromFile(file).close()

    // build and return the link
    val scopes = "user-read-private user-read-email"
    val state = "no-crossrefs"
    s"https://accounts.spotify.com/authorize?client_id=$clientId&response_type=code&redirect_uri=$redirectUri&scope=$scopes&state=$state"
  }

  // logs in the user, redirects to the redirect link, and returns the auth code 
  def getAuthCode(loginLink: String): Future[String] = {
        // Step 1: Print the login link for the user
    println(s"Log in using this link: $loginLink")

    // Step 2: Run the AuthListener server (this is non-blocking and will run in the background)
    AuthListener.run(List.empty).unsafeRunAndForget()
    
    // Step 3: Set up a promise that will eventually hold the auth code
    val promise = Promise[String]()

    // Step 4: Use a Future to capture the auth code once available
    Future {
      // `captureAuthCode()` returns an `IO[String]`, we need to convert it to a Future
      val authCodeIO: IO[String] = AuthListener.captureAuthCode()

      // Convert the IO to a Scala Future
      val authCodeFuture: Future[String] = authCodeIO.unsafeToFuture()

      // When the auth code is ready, complete the promise
      authCodeFuture.onComplete {
        case scala.util.Success(authCode) =>
          promise.success(authCode)  // Fulfill the promise with the auth code
        case scala.util.Failure(ex) =>
          promise.failure(ex)  // Handle any failure in getting the auth code
      }
    }

    // Return the Future that will hold the auth code
    promise.future
  }

  // Exchanges the auth code for an access token
  def exchangeAuthCodeForToken(authCode: String): String = {
    // Read the client id and secret from a file
    val file = "/Users/seancoughlin/projects/hiitbeats/src/backend/api_creds.txt"
    val lines = Source.fromFile(file).getLines.toArray
    val clientId = lines(0)
    val clientSecret = lines(1)
    Source.fromFile(file).close()

    // Set up the request
    val base64Auth = Base64.getEncoder.encodeToString(s"$clientId:$clientSecret".getBytes)
    val authHeader = Map("Authorization" -> s"Basic $base64Auth")
    val authBody = Map("grant_type" -> "authorization_code", "code" -> authCode, "redirect_uri" -> "http://localhost:9000/spotify/callback")
    val backend = HttpClientSyncBackend()

    // Send the request
    val response = basicRequest
      .post(uri"https://accounts.spotify.com/api/token")
      .headers(authHeader)
      .body(authBody)
      .send(backend)
    backend.close()

    // Parse the response
    val jsonString = response.body match {
      case Left(value) => value
      case Right(value) => value
    }
    val json = Json.parse(jsonString)

    // Extract the token from the response
    (json \ "access_token").as[String]
  }
}










