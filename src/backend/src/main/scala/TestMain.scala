package HiitBeats

import scala.util.{Try, Success, Failure}

object TestMain extends App {
    // Get user input
    println("Enter your interval length: ")
    val intervalLength = scala.io.StdIn.readLine().toInt
    println("Enter your rest length: ")
    val restLength = scala.io.StdIn.readLine().toInt
    println("Enter your workout length: ")
    val workoutLength = scala.io.StdIn.readLine().toInt
    val workout = ApiUtils.fillWorkout(intervalLength, restLength, workoutLength)

    // Get the token from the Spotify API
    val token: String = ApiUtils.getToken match {
      case Success(token) => token
      case Failure(exception) => throw exception 
    }
    // Get the songs
    val songs = ApiUtils.findSongs(token)

    // Match the songs to the workout
    val matchedSongs = ApiUtils.matchSongs(workout, songs)
    println(matchedSongs)
}
