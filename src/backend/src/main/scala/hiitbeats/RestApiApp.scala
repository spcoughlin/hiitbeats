package hiitbeats

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import scala.io.StdIn
import hiitbeats.impl.LoginApiImpl
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

object RestApiApp {
  def main(args: Array[String]): Unit = {
    // Create an ActorSystem to host the application
    implicit val system = ActorSystem("rest-api-system")
    implicit val executionContext = system.dispatcher

    // Define case classes for JSON serialization/deserialization
    case class LoginLinkResponse(loginLink: String)
    implicit val loginLinkResponseFormat = jsonFormat1(LoginLinkResponse)

    case class AuthCodeRequest(authCode: String)
    implicit val authCodeRequestFormat = jsonFormat1(AuthCodeRequest)

    case class UserIdResponse(userId: String)
    implicit val userIdResponseFormat = jsonFormat1(UserIdResponse)

    case class ErrorResponse(error: String)
    implicit val errorResponseFormat = jsonFormat1(ErrorResponse)

    case class MessageResponse(message: String)
    implicit val messageResponseFormat = jsonFormat1(MessageResponse)

    // Define the route
    val route =
      logRequestResult("Server") {
        path("login-link") {
          get {
            val loginLink = LoginApiImpl.getLoginLink
            complete(LoginLinkResponse(loginLink))
          }
        } ~
        path("exchange-get-user") {
          post {
            entity(as[AuthCodeRequest]) { authCodeRequest =>
              val authCode = authCodeRequest.authCode
              if (authCode.trim.isEmpty) {
                complete(StatusCodes.BadRequest, ErrorResponse("authCode cannot be empty"))
              } else {
                // Call the provided functions
                try {
                  val accessToken = LoginApiImpl.exchangeAuthCodeForToken(authCode)
                  val userId = LoginApiImpl.getUserID(accessToken)
                  complete(UserIdResponse(userId))
                } catch {
                  case e: Exception =>
                    // Log the exception and respond with a generic error message
                    system.log.error("Error processing request", e)
                    complete(StatusCodes.InternalServerError, ErrorResponse("An error occurred while processing your request."))
                }
              }
            }
          }
        } ~
        pathSingleSlash {
          get {
            complete(MessageResponse("Welcome to the API"))
          }
        }
      }

    // Start the server
    val bindingFuture = Http().newServerAt("localhost", 9000).bind(route)

    println("Server online at http://localhost:9000/\nPress RETURN to stop...")

    // Keep the server running until user presses RETURN
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete { _ =>
        system.terminate()
        println("Server stopped")
      }
  }
}

