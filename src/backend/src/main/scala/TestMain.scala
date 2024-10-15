package HiitBeats

import java.net.URLEncoder
import scala.io.StdIn.readLine
import scala.util.{Try, Failure, Success}

object TestMain extends App {
  // Log the user in and get the access token
  val loginLink: String = LoginApi.getLoginLink
  val authCode: String = LoginApi.getAuthCode(loginLink)
  val accessToken: String = LoginApi.exchangeAuthCodeForToken(authCode)

  // Get the user ID
  val userID: String = LoginApi.getUserID(accessToken)

  // Get user workout data
  println("Enter your total workout length: ")
  val workoutLength: Int = readLine().toInt

  println("Enter your active period length: ")
  val activeLength: Int = readLine().toInt

  println("Enter your rest period length: ")
  val restLength: Int = readLine().toInt

  val workout: List[Int] = ApiUtils.fillWorkout(activeLength, restLength, workoutLength)

  // Get the client Token
  val clientToken: String = ApiUtils.getClientToken match {
    case Success(value) => value
    case Failure(exception) => throw exception
  }

  // Get the workout songs query
  println("Enter your songs search query: ")
  val songsQuery: String = readLine().toString

  // Find songs
  val songs: List[Song] = ApiUtils.findSongs(clientToken, songsQuery)

  // Match songs to workout
  val uriString: String = ApiUtils.matchSongs(workout, songs)

  // TODO: Add the playlist to the user's account
  val playlistID: String = ApiUtils.makeUserPlaylist(userID, accessToken)

  // Add the songs to the Playlist
  ApiUtils.addSongsToPlaylist(playlistID, accessToken, uriString)
}

