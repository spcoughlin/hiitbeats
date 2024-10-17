package hiitbeats.api

import scala.util.Try

/**
 * An abstract class defining the interface for logging into the Spotify API.
 */
abstract class LoginApi extends BaseApi {

  /**
   * Gets the client token from Spotify API.
   *
   * @return A `Try[String]` containing the client token.
   */
  def getClientToken: Try[String]


  /**
   * Returns the login link for the user to log in.
   *
   * @return A `String` representing the login URL.
   */
  def getLoginLink: String

  /**
   * Retrieves the authorization code after the user logs in.
   *
   * @param loginLink The login URL to direct the user to.
   * @return The authorization code as a `String`.
   */
  def getAuthCode(loginLink: String): String

  /**
   * Exchanges the authorization code for an access token.
   *
   * @param authCode The authorization code received from the user login.
   * @return The access token as a `String`.
   */
  def exchangeAuthCodeForToken(authCode: String): String

  /**
   * Retrieves the user's Spotify ID using the access token.
   *
   * @param accessToken The access token obtained from Spotify.
   * @return The user's Spotify ID as a `String`.
   */
  def getUserID(accessToken: String): String
}

