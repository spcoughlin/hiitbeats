package HiitBeats

import java.util.Base64
import play.api.libs.json._
import scala.annotation.tailrec
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
}










