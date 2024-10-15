package HiitBeats

import java.net.URLEncoder
import scala.io.StdIn.readLine
import scala.util.{Failure, Success}

object TestMain extends App {
  // Log the user in and get the access token
  val loginLink = LoginApi.getLoginLink
  val authCode = LoginApi.getAuthCode(loginLink)
  val accessToken = LoginApi.exchangeAuthCodeForToken(authCode)
  println(s"\nAccess Token: $accessToken")

  // Get the user ID
  val userID = LoginApi.getUserID(accessToken)

  // Get user workout data
  println("Enter your total workout length: ")
  val workoutLength = readLine().toInt

  println("Enter your active period length: ")
  val activeLength = readLine().toInt

  println("Enter your rest period length: ")
  val restLength = readLine().toInt

  val workout = ApiUtils.fillWorkout(activeLength, restLength, workoutLength)

  // Get the client Token
  val clientToken = ApiUtils.getClientToken match {
    case Success(value) => value
    case Failure(exception) => throw exception
  }

  // Find songs
  val songs = ApiUtils.findSongs(clientToken)

  // Match songs to workout
  val uriString = ApiUtils.matchSongs(workout, songs)

  // TODO: Add the playlist to the user's account
  val playlistID = ApiUtils.makeUserPlaylist(userID, accessToken)
  println(s"Playlist ID: $playlistID")

  // Add the songs to the Playlist
  ApiUtils.addSongsToPlaylist(playlistID, accessToken, uriString)
}

