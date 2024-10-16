package hiitbeats

import hiitbeats.impl.LoginApiImpl
import hiitbeats.impl.ApiUtils
import hiitbeats.models.Song
import scala.io.StdIn.readLine
import scala.util.{Try, Failure, Success}
import sttp.client4.UriContext

object TestMain extends App {
  // Log the user in and get the access token
  val loginLink: String = LoginApiImpl.getLoginLink
  val authCode: String = LoginApiImpl.getAuthCode(loginLink)
  val accessToken: String = LoginApiImpl.exchangeAuthCodeForToken(authCode)

  // Get the user ID
  val userID: String = LoginApiImpl.getUserID(accessToken)

  // Get user workout data
  println("Enter your total workout length (in minutes): ")
  val workoutLength: Int = readLine().toInt

  println("Enter your active period length (in minutes): ")
  val activeLength: Int = readLine().toInt

  println("Enter your rest period length (in minutes): ")
  val restLength: Int = readLine().toInt

  val workout: List[Int] = ApiUtils.fillWorkout(activeLength, restLength, workoutLength)

  // Get the client token
  val clientToken: String = ApiUtils.getClientToken match {
    case Success(value)    => value
    case Failure(exception) => throw exception
  }

  // Get the workout songs query
  println("Enter your songs search query: ")
  val songsQuery: String = readLine()

  // Find songs
  val songs: List[Song] = ApiUtils.findSongs(clientToken, songsQuery)

  // Match songs to workout
  val uriString: String = ApiUtils.matchSongs(workout, songs)

  // Create a playlist on the user's account
  val playlistID: String = ApiUtils.makeUserPlaylist(userID, accessToken)

  // Add the songs to the playlist
  ApiUtils.addSongsToPlaylist(playlistID, accessToken, uriString)
}

