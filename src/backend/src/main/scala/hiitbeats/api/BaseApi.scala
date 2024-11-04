package hiitbeats.api

import java.nio.file.{Files, Paths}
import play.api.libs.json._
import scala.io.Source
import scala.io.Source
import scala.util.Try
import sttp.client4._
import sttp.client4.httpclient.HttpClientSyncBackend
import sttp.model.Uri


/**
 * Base API class containing shared methods for API interactions.
 */
abstract class BaseApi {

  /**
   * Makes an API request using the specified parameters.
   *
   * @param uri     The URI of the API endpoint.
   * @param headers The headers to include in the request.
   * @param body    Optional body of the request.
   * @param method  HTTP method (GET, POST, PUT, DELETE).
   * @return A `Try[String]` containing the response body or an exception.
   */
  def makeRequest(
      uri: Uri,
      headers: Map[String, String],
      body: Option[Any] = None,
      method: String = "GET"
  ): Try[String] = Try {
    val backend = HttpClientSyncBackend()
    val requestBase = method.toUpperCase match {
      case "POST"   => basicRequest.post(uri).headers(headers)
      case "GET"    => basicRequest.get(uri).headers(headers)
      case "PUT"    => basicRequest.put(uri).headers(headers)
      case "DELETE" => basicRequest.delete(uri).headers(headers)
      case _        => throw new IllegalArgumentException(s"Unsupported HTTP method: $method")
    }

    val request = body match {
      case Some(b: String)              => requestBase.body(b)
      case Some(b: Map[String, String]) => requestBase.body(b)
      case Some(b: JsValue)             => requestBase.body(b.toString())
      case Some(b)                      => requestBase.body(b.toString)
      case None                         => requestBase
    }

    val response = request.send(backend)

    response.body match {
      case Left(error)  => throw new Exception(error)
      case Right(value) => value
    }
  }

  /**
    * Retrieves credentials from a file.
    *
    * @param filePath The path to the credentials file, with `~` optionally representing the home directory.
    * @return A `Map[String, String]` containing the credentials.
    */
  def getCredentials(filePath: String): Map[String, String] = {
    // Expand '~' to the user's home directory
    val expandedPath = if (filePath.startsWith("~")) {
      Paths.get(System.getProperty("user.home"), filePath.drop(1)).toString
    } else {
      filePath
    }

    // Check if the file exists
    if (Files.exists(Paths.get(expandedPath))) {
      val source = Source.fromFile(expandedPath)
      try {
        val lines = source.getLines().toArray
        Map(
          "clientId"     -> lines(0),
          "clientSecret" -> lines(1),
          "redirectUri"  -> lines(2)
        )
      } finally {
        source.close()
      }
    } else {
      throw new java.io.FileNotFoundException(s"File not found at path: $expandedPath")
    }
  }
  /**
   * Parses a JSON string into a `JsValue`.
   *
   * @param response The JSON string to parse.
   * @return A `JsValue` representing the parsed JSON.
   */
  def parseJson(response: String): JsValue = Json.parse(response)
}

