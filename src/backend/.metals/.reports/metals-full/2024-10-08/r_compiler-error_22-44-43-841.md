file://<WORKSPACE>/src/main/scala/ApiUtils.scala
### file%3A%2F%2F%2FUsers%2Fseancoughlin%2Fprojects%2Fhiitbeats%2Fsrc%2Fbackend%2Fsrc%2Fmain%2Fscala%2FApiUtils.scala:81: error: illegal start of definition `def`
  def main(args: Array[String]): Unit = {
  ^

occurred in the presentation compiler.

presentation compiler configuration:
Scala version: 2.12.19
Classpath:
<HOME>/Library/Caches/Coursier/v1/https/repo1.maven.org/maven2/org/scala-lang/scala-library/2.12.19/scala-library-2.12.19.jar [exists ]
Options:



action parameters:
uri: file://<WORKSPACE>/src/main/scala/ApiUtils.scala
text:
```scala
import scala.io.Source
import java.util.Base64
import sttp.client4._
import scala.annotation.tailrec
import sttp.client4.httpclient.HttpClientSyncBackend
import play.api.libs.json._

object ApiUtils {

  // Case class to store what we want from a Spotify Song
  case class Song(name: String, artist: String, duration: Int, uri: String)

  /* Gets the authentication token from the Spotify API
   * No args, returns String
   */
  def getToken(): String = {
    val file = "<WORKSPACE>/api_creds.txt"
    val lines = Source.fromFile(file).getLines.toArray
    val clientId = lines(0)
    val clientSecret = lines(1)
    val base64Auth = Base64.getEncoder.encodeToString(s"$clientId:$clientSecret".getBytes)
    val authHeader = Map("Authorization" -> s"Basic $base64Auth")
    val authBody = Map("grant_type" -> "client_credentials")
    val backend = HttpClientSyncBackend()
    val response = basicRequest
      .post(uri"https://accounts.spotify.com/api/token")
      .headers(authHeader)
      .body(authBody)
      .send(backend)
    backend.close()
    val jsonString = response.body match {
      case Left(value) => value
      case Right(value) => value
    }
    val json = Json.parse(jsonString)
    (json \ "access_token").as[String]

  }

  /* Gets the songs data from the Spotify API
   * Takes a token: String, returns List[Song]
   */
  def findSongs(token : String): List[Song] = {
    val url = "https://api.spotify.com/v1/search"
    val header = Map("Authorization" -> s"Bearer $token")
    val params = Map("query" -> "kanye", "limit" -> 50, "type" -> "track")
    val backend = HttpClientSyncBackend()
    val response = basicRequest
      .post(url)
      .headers(header)
      .body(params)
      .send(backend)
    backend.close
    val jsonString = response.body match {
      case Left(value) => value
      case Right(value) => value
    }
    val json = Json.parse(jsonString)
    val rawSongs: List[TrackObject] = (json \ "tracks" \ "items")
    def tuplize(e: TrackObject): Song = {
      val name: Option[String] = (e \ name)
      val artist: Option[String] = (e \ "artists")(0) \ "name" match {
        case JsDefined(JsString(name)) => Some(name)
        case _ => None
      }
      val duration: Option[Int] = (e \ "duration_ms")
      val uri: Option[String] = (e \ "uri")
      (name, artist, duration, uri) match {
        case (Some(name), Some(artist), Some(duration), Some(uri)) => Song(name, artist, duration, uri)
        case _ => Song("Error", "Error", 0, "Error")
      }
    }
      


    }

    
  }

  def main(args: Array[String]): Unit = {
    println(getToken())
  }

}

```



#### Error stacktrace:

```
scala.meta.internal.parsers.Reporter.syntaxError(Reporter.scala:16)
	scala.meta.internal.parsers.Reporter.syntaxError$(Reporter.scala:16)
	scala.meta.internal.parsers.Reporter$$anon$1.syntaxError(Reporter.scala:22)
	scala.meta.internal.parsers.Reporter.syntaxError(Reporter.scala:17)
	scala.meta.internal.parsers.Reporter.syntaxError$(Reporter.scala:17)
	scala.meta.internal.parsers.Reporter$$anon$1.syntaxError(Reporter.scala:22)
	scala.meta.internal.parsers.ScalametaParser.statSeqBuf(ScalametaParser.scala:4109)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$statSeq$1(ScalametaParser.scala:4096)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$statSeq$1$adapted(ScalametaParser.scala:4096)
	scala.meta.internal.parsers.ScalametaParser.scala$meta$internal$parsers$ScalametaParser$$listBy(ScalametaParser.scala:562)
	scala.meta.internal.parsers.ScalametaParser.statSeq(ScalametaParser.scala:4096)
	scala.meta.internal.parsers.ScalametaParser.bracelessPackageStats$1(ScalametaParser.scala:4285)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$source$1(ScalametaParser.scala:4288)
	scala.meta.internal.parsers.ScalametaParser.atPos(ScalametaParser.scala:325)
	scala.meta.internal.parsers.ScalametaParser.autoPos(ScalametaParser.scala:369)
	scala.meta.internal.parsers.ScalametaParser.source(ScalametaParser.scala:4264)
	scala.meta.internal.parsers.ScalametaParser.entrypointSource(ScalametaParser.scala:4291)
	scala.meta.internal.parsers.ScalametaParser.parseSourceImpl(ScalametaParser.scala:119)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$parseSource$1(ScalametaParser.scala:116)
	scala.meta.internal.parsers.ScalametaParser.parseRuleAfterBOF(ScalametaParser.scala:58)
	scala.meta.internal.parsers.ScalametaParser.parseRule(ScalametaParser.scala:53)
	scala.meta.internal.parsers.ScalametaParser.parseSource(ScalametaParser.scala:116)
	scala.meta.parsers.Parse$.$anonfun$parseSource$1(Parse.scala:30)
	scala.meta.parsers.Parse$$anon$1.apply(Parse.scala:37)
	scala.meta.parsers.Api$XtensionParseDialectInput.parse(Api.scala:22)
	scala.meta.internal.semanticdb.scalac.ParseOps$XtensionCompilationUnitSource.toSource(ParseOps.scala:15)
	scala.meta.internal.semanticdb.scalac.TextDocumentOps$XtensionCompilationUnitDocument.toTextDocument(TextDocumentOps.scala:161)
	scala.meta.internal.pc.SemanticdbTextDocumentProvider.textDocument(SemanticdbTextDocumentProvider.scala:54)
	scala.meta.internal.pc.ScalaPresentationCompiler.$anonfun$semanticdbTextDocument$1(ScalaPresentationCompiler.scala:469)
```
#### Short summary: 

file%3A%2F%2F%2FUsers%2Fseancoughlin%2Fprojects%2Fhiitbeats%2Fsrc%2Fbackend%2Fsrc%2Fmain%2Fscala%2FApiUtils.scala:81: error: illegal start of definition `def`
  def main(args: Array[String]): Unit = {
  ^