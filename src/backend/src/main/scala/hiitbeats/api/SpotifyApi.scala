package hiitbeats.api

import scala.util.Try
import hiitbeats.models.Song
import hiitbeats.models.Playlist

/**
 * An abstract class defining the Spotify API interface.
 */
abstract class SpotifyApi extends BaseApi {

  /**
   * Fills a workout with intervals and rest periods.
   *
   * @param intLength   The length of the interval in minutes.
   * @param restLength  The length of the rest period in minutes.
   * @param totalLength The total length of the workout in minutes.
   * @return A list of intervals and rest periods in milliseconds.
   */
  def fillWorkout(intLength: Int, restLength: Int, totalLength: Int): List[Int]

  /**
   * Finds songs from Spotify API.
   *
   * @param token The user's access token.
   * @param query The search query.
   * @return A list of `Song` objects.
   */
  def songSearch(token: String, query: String): List[Song]

  /**
   * Gets the user's top 50 playlists.
   *
   * @param token The user's access token.
   * @return A list of Playlist objects. 
   */
  def getTop50Playlists(token: String): List[Playlist]

  /**
   * Gets songs from a playlist.
   *
   * @param token      The user's access token.
   * @param playlistID The playlist ID.
   * @return A list of `Song` objects.
   */
  def songsFromPlaylist(token: String, playlistID: String): List[Song]

  /**
   * Matches songs to workout intervals.
   *
   * @param workout A list of workout intervals.
   * @param songs   A list of songs.
   * @return A comma-separated string of song URIs.
   */
  def matchSongs(workout: List[Int], songs: List[Song]): String

  /**
   * Creates a playlist on a user's account.
   *
   * @param userID      The user's Spotify ID.
   * @param accessToken The user's access token.
   * @return The playlist ID as a `String`.
   */
  def makeUserPlaylist(userID: String, accessToken: String): String

  /**
   * Adds songs to a playlist.
   *
   * @param playlistID  The playlist ID.
   * @param accessToken The user's access token.
   * @param uris        The URIs of the songs to add.
   */
  def addSongsToPlaylist(playlistID: String, accessToken: String, uris: String): Unit
}

