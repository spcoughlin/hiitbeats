package HiitBeats

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import play.api.libs.json._
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.io.Source
import scala.language.postfixOps
import scala.util.{Try, Success, Failure}
import sttp.client4._
import sttp.client4.httpclient.HttpClientSyncBackend
import sttp.model.Uri


// Case class to store what we want from a Spotify Song
case class Song(name: String, 
                artist: String, 
                duration: Int, 
                uri: String)

// Object to hold the Spotify API functions
object ApiUtils extends SpotifyApi {

  override def getClientToken: Try[String] = Try {
    val (clientId, clientSecret) = getCredentials("/Users/seancoughlin/projects/hiitbeats/src/backend/api_creds.txt")
    val base64Auth = Base64.getEncoder.encodeToString(s"$clientId:$clientSecret".getBytes)
    val headers = Map(
      "Authorization" -> s"Basic $base64Auth",
      "Content-Type" -> "application/x-www-form-urlencoded"
    )
    val body = "grant_type=client_credentials"

    val response = makeRequest(uri"https://accounts.spotify.com/api/token", headers, Some(body)).get
    val json = parseJson(response)
    (json \ "access_token").as[String]
  }

  override def findSongs(token: String, query: String): List[Song] = {
    val headers = Map("Authorization" -> s"Bearer $token")
    val params = Map("q" -> query, "limit" -> "50", "type" -> "track")
    val uriWithParams: Uri = uri"https://api.spotify.com/v1/search".params(params)

    val response = makeRequest(uriWithParams, headers).get
    val json = parseJson(response)
    val rawSongs: Option[JsArray] = (json \ "tracks" \ "items").asOpt[JsArray]

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

    rawSongs match {
      case Some(songs) => songs.value.toList.map(e => tuplize(e))
      case None => List(Song("Error", "Error", 0, "Error"))
    }
  }

  override def fillWorkout(intLength: Int, restLength: Int, totalLength: Int): List[Int] = {
    @tailrec
    def fillWorkoutHelper(intLength: Int, restLength: Int, totalLength: Int, acc: List[Int]): List[Int] = {
      if (totalLength <= 0) acc
      else if (totalLength - intLength <= 0) acc :+ totalLength
      else fillWorkoutHelper(intLength, restLength, totalLength - intLength - restLength, acc :+ (restLength * 60000) :+ (intLength * 60000))
    }
    fillWorkoutHelper(intLength, restLength, totalLength, List())
  }  
  
  override def matchSongs(workout: List[Int], songs: List[Song]): String = {
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

  override def makeUserPlaylist(userID: String, accessToken: String): String = {
    // For the name of the playlist, get the current date and time
    val now = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")
    val formattedDateTime = now.format(formatter)

    val headers = Map("Authorization" -> s"Bearer $accessToken", "Content-Type" -> "application/json")
    val body = Json.obj(
      "name" -> s"HiitBeats Workout $formattedDateTime",
      "description" -> "Playlist for your HIIT workout",
      "public" -> false
    ).toString()

    val uri = uri"https://api.spotify.com/v1/users/$userID/playlists"
    val response = makeRequest(uri, headers, Some(body)).get
    val json = parseJson(response)
    (json \ "id").asOpt[String].getOrElse("Error")
  }

  override def addSongsToPlaylist(playlistID: String, accessToken: String, uris: String): Unit = {
    val headers = Map(
      "Authorization" -> s"Bearer $accessToken",
      "Content-Type" -> "application/json"
    )
    val uriList = uris.split(",").toList
    val body = Json.obj(
      "uris" -> uriList
    ).toString()
    val uri = uri"https://api.spotify.com/v1/playlists/$playlistID/tracks"

    val response = makeRequest(uri, headers, Some(body)).get
    if (response.nonEmpty) {
      println("Playlist updated successfully!")
    } else {
      println("Failed to add songs to the playlist.")
    }
  }
}

