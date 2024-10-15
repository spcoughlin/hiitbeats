package HiitBeats

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

abstract class SpotifyApi {
  /* 
   * Gets the client token from Spotify API
   * @return Try[String] - the client token
   */
  def getClientToken: Try[String]

  /* 
   * Fills a workout with intervals and rest periods
   * @param intLength: Int - the length of the interval in minutes
   * @param restLength: Int - the length of the rest period in minutes
   * @param totalLength: Int - the total length of the workout in minutes
   * @return List[Int] - a list of intervals and rest periods in milliseconds
   */
  def fillWorkout(intLength: Int, restLength: Int, totalLength: Int): List[Int]

  /* 
   * Finds songs from Spotify API
   * @param token: String - the client token
   * @param query: String - the search query
   * @return List[Song] - a list of songs
   */
  def findSongs(token: String, query: String): List[Song]

  /* 
   * Matches songs to workout intervals
   * @param workout: List[Int] - the workout intervals
   * @param songs: List[Song] - the list of songs
   * @return String - a string of song URIs
   */
  def matchSongs(workout: List[Int], songs: List[Song]): String

  /* 
   * Creates a playlist on a user's account
   * @param userID: String - the user's ID
   * @param accessToken: String - the user's access token
   * @return String - the playlist ID
   */
  def makeUserPlaylist(userID: String, accessToken: String): String

  /* 
   * Adds songs to a playlist
   * @param playlistID: String - the playlist ID
   * @param accessToken: String - the user's access token
   * @param uris: String - the URIs of the songs to add
   */
  def addSongsToPlaylist(playlistID: String, accessToken: String, uris: String): Unit

  // Abstract method for making API requests
  def makeRequest(uri: Uri, headers: Map[String, String], body: Option[String] = None): Try[String] = Try {
    val backend = HttpClientSyncBackend()
    val response = body match {
      case Some(b) => basicRequest
        .post(uri)
        .headers(headers)
        .body(b)
        .send(backend)
      case None => basicRequest
        .get(uri)
        .headers(headers)
        .send(backend)
    }

    // Return response body
    response.body match {
      case Left(value) => throw new Exception(value)
      case Right(value) => value
    }
  }

  // Helper method to get credentials from a file
  def getCredentials(filePath: String): (String, String) = {
    val lines = Source.fromFile(filePath).getLines.toArray
    Source.fromFile(filePath).close()
    (lines(0), lines(1)) // Return clientId, clientSecret
  }

  // Helper method to parse JSON responses
  def parseJson(response: String): JsValue = Json.parse(response)
}

