package hiitbeats

import hiitbeats.impl.LoginApiImpl
import hiitbeats.impl.ApiUtils
import hiitbeats.models.Song
import hiitbeats.models.Playlist
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
  val clientToken: String = LoginApiImpl.getClientToken match {
    case Success(value)     => value
    case Failure(exception) => throw exception
  }

  // Ask user to choose between searching for songs or using a playlist
  println("Choose an option:")
  println("1. Search for songs")
  println("2. Use songs from a playlist")
  val choice: String = readLine()

  val songs: List[Song] = choice match {
    case "1" =>
      // Get the workout songs query
      println("Enter your songs search query: ")
      val songsQuery: String = readLine()

      // Find songs
      ApiUtils.songSearch(accessToken, songsQuery)

    case "2" =>
      // Get user's top 5 playlists
      val playlists: List[Playlist] = ApiUtils.getTop50Playlists(accessToken)

      if (playlists.isEmpty) {
        println("No playlists found. Exiting.")
        sys.exit(1)
      }

      // Display the playlists
      println("Your top 5 playlists:")
      playlists.zipWithIndex.foreach { case (playlist, index) =>
        println(s"${index + 1}. ${playlist.name}")
      }

      // Ask the user to choose a playlist
      println("Enter the number of the playlist you want to use: ")
      val playlistChoice: Int = readLine().toIntOption match {
        case Some(num) if num >= 1 && num <= playlists.length => num
        case _ =>
          println("Invalid choice. Exiting.")
          sys.exit(1)
      }

      val selectedPlaylist = playlists(playlistChoice - 1)
      println(s"You selected: ${selectedPlaylist.name}")

      // Get songs from the selected playlist
      ApiUtils.songsFromPlaylist(accessToken, selectedPlaylist.id)
  }

  // Match songs to workout
  val uriString: String = ApiUtils.matchSongs(workout, songs)

  // Create a playlist on the user's account
  val playlistID: String = ApiUtils.makeUserPlaylist(userID, accessToken)

  // Add the songs to the playlist
  ApiUtils.addSongsToPlaylist(playlistID, accessToken, uriString)
}

