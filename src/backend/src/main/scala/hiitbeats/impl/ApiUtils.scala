package hiitbeats.impl

import hiitbeats.api.SpotifyApi
import hiitbeats.models.Song
import hiitbeats.models.Playlist
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import play.api.libs.json._
import scala.annotation.tailrec
import scala.util.{Try, Success, Failure}
import sttp.model.Uri
import sttp.client4._


object ApiUtils extends SpotifyApi {

  override def getClientToken: Try[String] = Try {
    val creds = getCredentials("/Users/seancoughlin/projects/hiitbeats/src/backend/api_creds.txt")
    val clientId = creds("clientId")
    val clientSecret = creds("clientSecret")
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

  override def songSearch(token: String, query: String): List[Song] = {
    val headers = Map("Authorization" -> s"Bearer $token")
    val params = Map("q" -> query, "limit" -> "50", "type" -> "track")
    val uriWithParams: Uri = uri"https://api.spotify.com/v1/search".params(params)

    val response = makeRequest(uriWithParams, headers, method = "GET").get
    val json = parseJson(response)
    val rawSongs: Option[JsArray] = (json \ "tracks" \ "items").asOpt[JsArray]

    val songs1 = rawSongs match {
      case Some(songs) => songs.value.toList.map { e =>
        val name = (e \ "name").asOpt[String].getOrElse("Unknown")
        val artist = (e \ "artists")(0) \ "name" match {
          case JsDefined(JsString(artistName)) => artistName
          case _                               => "Unknown"
        }
        val duration = (e \ "duration_ms").asOpt[Int].getOrElse(0)
        val uri = (e \ "uri").asOpt[String].getOrElse("")
        Song(name, artist, duration, uri)
      }
      case None => List(Song("Error", "Error", 0, "Error"))
    }

    // do another request to get the next 50 songs
    val params2 = Map("q" -> query, "limit" -> "50", "type" -> "track", "offset" -> "50")
    val uriWithParams2: Uri = uri"https://api.spotify.com/v1/search".params(params2)

    val response2 = makeRequest(uriWithParams2, headers, method = "GET").get
    val json2 = parseJson(response2)
    val rawSongs2: Option[JsArray] = (json2 \ "tracks" \ "items").asOpt[JsArray]

    val songs2 = rawSongs2 match {
      case Some(songs) => songs.value.toList.map { e =>
        val name = (e \ "name").asOpt[String].getOrElse("Unknown")
        val artist = (e \ "artists")(0) \ "name" match {
          case JsDefined(JsString(artistName)) => artistName
          case _                               => "Unknown"
        }
        val duration = (e \ "duration_ms").asOpt[Int].getOrElse(0)
        val uri = (e \ "uri").asOpt[String].getOrElse("")
        Song(name, artist, duration, uri)
      }
      case None => List(Song("Error", "Error", 0, "Error"))
    }
    songs1 ++ songs2
  }

  override def getTop5Playlists(token: String): List[Playlist] = {
    val headers = Map("Authorization" -> s"Bearer $token")
    val params = Map("limit" -> "5")
    val uri = uri"https://api.spotify.com/v1/me/playlists".params(params)

    val response = makeRequest(uri, headers, method = "GET").get
    val json = parseJson(response)
    val rawPlaylists: Option[JsArray] = (json \ "items").asOpt[JsArray]

    rawPlaylists match {
      case Some(playlists) => playlists.value.toList.map { e =>
        val name = (e \ "name").asOpt[String].getOrElse("Unknown")
        val id = (e \ "id").asOpt[String].getOrElse("Unknown")
        Playlist(id, name)
      }
      case None => List(Playlist("Error", "Error"))
    }
  }

  override def songsFromPlaylist(token: String, playlistID: String): List[Song] = {
    val headers = Map("Authorization" -> s"Bearer $token")
    val params1 = Map("limit" -> "50")
    val uri = uri"https://api.spotify.com/v1/playlists/$playlistID/tracks".params(params1)

    val response = makeRequest(uri, headers, method = "GET").get
    val json = parseJson(response)
    val rawSongs: Option[JsArray] = (json \ "items").asOpt[JsArray]

    val songs1 = rawSongs match {
      case Some(songs) => songs.value.toList.map { e =>
        val name = (e \ "track" \ "name").asOpt[String].getOrElse("Unknown")
        val artist = (e \ "track" \ "artists")(0) \ "name" match {
          case JsDefined(JsString(artistName)) => artistName
          case _                               => "Unknown"
        }
        val duration = (e \ "track" \ "duration_ms").asOpt[Int].getOrElse(0)
        val uri = (e \ "track" \ "uri").asOpt[String].getOrElse("")
        Song(name, artist, duration, uri)
      }
      case None => List(Song("Error", "Error", 0, "Error"))
    }

    // get the next 50 songs from the playlist
    val body2 = Map("limit" -> "50", "offset" -> "50")
    val uri2 = uri"https://api.spotify.com/v1/playlists/$playlistID/tracks".params(body2)
    val response2 = makeRequest(uri, headers, method = "GET").get
    val json2 = parseJson(response2)
    val rawSongs2: Option[JsArray] = (json2 \ "items").asOpt[JsArray]

    val songs2 = rawSongs2 match {
      case Some(songs) => songs.value.toList.map { e =>
        val name = (e \ "track" \ "name").asOpt[String].getOrElse("Unknown")
        val artist = (e \ "track" \ "artists")(0) \ "name" match {
          case JsDefined(JsString(artistName)) => artistName
          case _                               => "Unknown"
        }
        val duration = (e \ "track" \ "duration_ms").asOpt[Int].getOrElse(0)
        val uri = (e \ "track" \ "uri").asOpt[String].getOrElse("")
        Song(name, artist, duration, uri)
      }
      case None => List(Song("Error", "Error", 0, "Error"))
    }
    songs1 ++ songs2
  }

  override def fillWorkout(intLength: Int, restLength: Int, totalLength: Int): List[Int] = {
    @tailrec
    def fillWorkoutHelper(remainingTime: Int, acc: List[Int]): List[Int] = {
      if (remainingTime <= 0) acc
      else if (remainingTime - intLength <= 0) acc :+ (remainingTime * 60000)
      else fillWorkoutHelper(remainingTime - intLength - restLength, acc :+ (intLength * 60000) :+ (restLength * 60000))
    }
    fillWorkoutHelper(totalLength, List())
  }  
  
  override def matchSongs(workout: List[Int], songs: List[Song]): String = {
    def getClosestSong(interval: Int, songs: List[Song]): Song = {
      songs.minBy(s => Math.abs(s.duration - interval))
    }
    @tailrec
    def matchSongsHelper(workout: List[Int], songs: List[Song], acc: List[String]): List[String] = {
      workout match {
        case Nil => acc
        case interval :: rest => 
          val closestSong = getClosestSong(interval, songs)
          println(s"Matching ${interval.toFloat / 60000.0} min to '${closestSong.name}' by ${closestSong.artist}")
          matchSongsHelper(rest, songs.filterNot(_ == closestSong), acc :+ closestSong.uri)
      }
    }
    matchSongsHelper(workout, songs, List()).mkString(",")
  }

  override def makeUserPlaylist(userID: String, accessToken: String): String = {
    // Get the current date and time for the playlist name
    val now = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm")
    val formattedDateTime = now.format(formatter)

    val headers = Map(
      "Authorization" -> s"Bearer $accessToken",
      "Content-Type"  -> "application/json"
    )
    val body = Json.obj(
      "name"        -> s"HiitBeats Workout $formattedDateTime",
      "description" -> "Playlist for your HIIT workout",
      "public"      -> false
    ).toString()

    val uri = uri"https://api.spotify.com/v1/users/$userID/playlists"
    val response = makeRequest(uri, headers, Some(body), method = "POST").get
    val json = parseJson(response)
    (json \ "id").asOpt[String].getOrElse("Error")
  }

  override def addSongsToPlaylist(playlistID: String, accessToken: String, uris: String): Unit = {
    val headers = Map(
      "Authorization" -> s"Bearer $accessToken",
      "Content-Type"  -> "application/json"
    )
    val uriList = uris.split(",").toList
    val body = Json.obj(
      "uris" -> uriList
    ).toString()
    val uri = uri"https://api.spotify.com/v1/playlists/$playlistID/tracks"

    val response = makeRequest(uri, headers, Some(body), method = "POST").get

    val jsonResponse = parseJson(response)
    (jsonResponse \ "snapshot_id").asOpt[String] match {
      case Some(_) => println("Playlist updated successfully!")
      case None    => println("Failed to add songs to the playlist.")
    }
  }
}

