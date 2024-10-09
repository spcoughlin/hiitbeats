file://<WORKSPACE>/src/main/scala/ApiUtils.scala
### file%3A%2F%2F%2FUsers%2Fseancoughlin%2Fprojects%2Fhiitbeats%2Fsrc%2Fbackend%2Fsrc%2Fmain%2Fscala%2FApiUtils.scala:105: error: illegal start of simple pattern
    val songs = findSongs(token)
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
import scala.util.{Try, Success, Failure}
import java.util.Base64
import sttp.client4._
import sttp.model.Uri
import scala.annotation.tailrec
import sttp.client4.httpclient.HttpClientSyncBackend
import play.api.libs.json._

object ApiUtils {

  // Case class to store what we want from a Spotify Song
  case class Song(name: String, artist: String, duration: Int, uri: String)

  /* Gets the authentication token from the Spotify API
   * No args, returns String
   */
  def getToken: Try[String] = Try {

    // Read the client id and secret from a file
    val file = "<WORKSPACE>/api_creds.txt"
    val lines = Source.fromFile(file).getLines.toArray
    val clientId = lines(0)
    val clientSecret = lines(1)
    
    // Set up the request
    val base64Auth = Base64.getEncoder.encodeToString(s"$clientId:$clientSecret".getBytes)
    val authHeader = Map("Authorization" -> s"Basic $base64Auth")
    val authBody = Map("grant_type" -> "client_credentials")
    val backend = HttpClientSyncBackend()
    
    // Send the request
    val response = basicRequest
      .post(uri"https://accounts.spotify.com/api/token")
      .headers(authHeader)
      .body(authBody)
      .send(backend)
    backend.close()

    // Parse the response
    val jsonString = response.body match {
      case Left(value) => value
      case Right(value) => value
    }
    val json = Json.parse(jsonString)

    // Extract the token from the response
    (json \ "access_token").as[String]

  }

  /* Gets the songs data from the Spotify API
   * Takes a token: String, returns List[Song]
   */
  def findSongs(token : String): List[Song] = {

    // Set up the request
    val header = Map("Authorization" -> s"Bearer $token")
    val params = Map("q" -> "kanye", "limit" -> "50", "type" -> "track")
    val uriWithParams: Uri = uri"https://api.spotify.com/v1/search"
      .params(params)
    val backend = HttpClientSyncBackend()

    // Send the request
    val response = basicRequest
      .get(uriWithParams)
      .headers(header)
      .send(backend)
    backend.close()

    // Parse the response
    val jsonString = response.body match {
      case Left(value) => value
      case Right(value) => value
    }
    val json = Json.parse(jsonString)

    // Extract the songs from the response
    val rawSongs: Option[JsArray] = (json \ "tracks" \ "items").asOpt[JsArray]

    // Helper function to convert a value to a Song
    def tuplize(e: JsValue): Song = {
      val name: Option[String] = (e \ "name").asOpt[String]
      val artist: Option[String] = (e \ "artists")(0) \ "name" match {
        case JsDefined(JsString(name)) => Some(name)
        case _ => None
      }
      val duration: Option[Int] = (e \ "duration_ms").asOpt[Int]
      val uri: Option[String] = (e \ "uri").asOpt[String]
      (name, artist, duration, uri) match {
        case (Some(name), Some(artist), Some(duration), Some(uri)) => Song(name, artist, duration, uri)
        case _ => Song("Error", "Error", 0, "Error")
      }
    }
    // map the rawSongs to a list of Songs and return
    rawSongs match {
      case Some(songs) => songs.value.toList.map(e => tuplize(e))
      case None => List(Song("Error", "Error", 0, "Error"))
    }
  }

  def main(args: Array[String]): Unit = {
    getToken() match {
      case 
    val songs = findSongs(token)
    songs.foreach(println)
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
	scala.meta.internal.parsers.ScalametaParser$SeqContextSensitive.badPattern3(ScalametaParser.scala:2641)
	scala.meta.internal.parsers.ScalametaParser$SeqContextSensitive.badPattern3$(ScalametaParser.scala:2621)
	scala.meta.internal.parsers.ScalametaParser$noSeq$.badPattern3(ScalametaParser.scala:2722)
	scala.meta.internal.parsers.ScalametaParser$SeqContextSensitive.$anonfun$pattern3$1(ScalametaParser.scala:2603)
	scala.meta.internal.parsers.ScalametaParser$SeqContextSensitive.$anonfun$simplePattern$1(ScalametaParser.scala:2708)
	scala.meta.internal.parsers.ScalametaParser.autoEndPos(ScalametaParser.scala:372)
	scala.meta.internal.parsers.ScalametaParser$SeqContextSensitive.simplePattern(ScalametaParser.scala:2650)
	scala.meta.internal.parsers.ScalametaParser$SeqContextSensitive.simplePattern$(ScalametaParser.scala:2644)
	scala.meta.internal.parsers.ScalametaParser$noSeq$.simplePattern(ScalametaParser.scala:2722)
	scala.meta.internal.parsers.ScalametaParser$SeqContextSensitive.pattern3(ScalametaParser.scala:2603)
	scala.meta.internal.parsers.ScalametaParser$SeqContextSensitive.pattern3$(ScalametaParser.scala:2601)
	scala.meta.internal.parsers.ScalametaParser$noSeq$.pattern3(ScalametaParser.scala:2722)
	scala.meta.internal.parsers.ScalametaParser$SeqContextSensitive.pattern2(ScalametaParser.scala:2582)
	scala.meta.internal.parsers.ScalametaParser$SeqContextSensitive.pattern2$(ScalametaParser.scala:2581)
	scala.meta.internal.parsers.ScalametaParser$noSeq$.pattern2(ScalametaParser.scala:2722)
	scala.meta.internal.parsers.ScalametaParser$SeqContextSensitive.pattern1(ScalametaParser.scala:2556)
	scala.meta.internal.parsers.ScalametaParser$SeqContextSensitive.pattern1$(ScalametaParser.scala:2555)
	scala.meta.internal.parsers.ScalametaParser$noSeq$.pattern1(ScalametaParser.scala:2722)
	scala.meta.internal.parsers.ScalametaParser$SeqContextSensitive.patternAlternatives(ScalametaParser.scala:2515)
	scala.meta.internal.parsers.ScalametaParser$SeqContextSensitive.pattern(ScalametaParser.scala:2511)
	scala.meta.internal.parsers.ScalametaParser$SeqContextSensitive.pattern$(ScalametaParser.scala:2511)
	scala.meta.internal.parsers.ScalametaParser$noSeq$.pattern(ScalametaParser.scala:2722)
	scala.meta.internal.parsers.ScalametaParser.pattern(ScalametaParser.scala:2741)
	scala.meta.internal.parsers.ScalametaParser.caseClause(ScalametaParser.scala:2414)
	scala.meta.internal.parsers.ScalametaParser.iter$5(ScalametaParser.scala:2442)
	scala.meta.internal.parsers.ScalametaParser.caseClausesIfAny(ScalametaParser.scala:2448)
	scala.meta.internal.parsers.ScalametaParser.caseClauses(ScalametaParser.scala:2425)
	scala.meta.internal.parsers.ScalametaParser.matchClause(ScalametaParser.scala:1410)
	scala.meta.internal.parsers.ScalametaParser.iter$3(ScalametaParser.scala:1592)
	scala.meta.internal.parsers.ScalametaParser.exprOtherRest(ScalametaParser.scala:1596)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$expr$2(ScalametaParser.scala:1553)
	scala.meta.internal.parsers.ScalametaParser.atPosOpt(ScalametaParser.scala:327)
	scala.meta.internal.parsers.ScalametaParser.autoPosOpt(ScalametaParser.scala:370)
	scala.meta.internal.parsers.ScalametaParser.expr(ScalametaParser.scala:1480)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$blockStatSeq$3(ScalametaParser.scala:4234)
	scala.meta.internal.parsers.ScalametaParser.stat(ScalametaParser.scala:4091)
	scala.meta.internal.parsers.ScalametaParser.iter$7(ScalametaParser.scala:4234)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$blockStatSeq$1(ScalametaParser.scala:4247)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$blockStatSeq$1$adapted(ScalametaParser.scala:4197)
	scala.meta.internal.parsers.ScalametaParser.scala$meta$internal$parsers$ScalametaParser$$listBy(ScalametaParser.scala:562)
	scala.meta.internal.parsers.ScalametaParser.blockStatSeq(ScalametaParser.scala:4197)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$blockOnBrace$2(ScalametaParser.scala:2385)
	scala.meta.internal.parsers.ScalametaParser.inBracesOnOpen(ScalametaParser.scala:265)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$blockOnBrace$1(ScalametaParser.scala:2383)
	scala.meta.internal.parsers.ScalametaParser.atPos(ScalametaParser.scala:325)
	scala.meta.internal.parsers.ScalametaParser.autoPos(ScalametaParser.scala:369)
	scala.meta.internal.parsers.ScalametaParser.blockOnBrace(ScalametaParser.scala:2383)
	scala.meta.internal.parsers.ScalametaParser.blockOnBrace(ScalametaParser.scala:2385)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$blockExprOnBrace$1(ScalametaParser.scala:2388)
	scala.meta.internal.parsers.ScalametaParser.blockExprPartial(ScalametaParser.scala:2367)
	scala.meta.internal.parsers.ScalametaParser.blockExprOnBrace(ScalametaParser.scala:2387)
	scala.meta.internal.parsers.ScalametaParser.simpleExpr0(ScalametaParser.scala:2079)
	scala.meta.internal.parsers.ScalametaParser.simpleExpr(ScalametaParser.scala:2061)
	scala.meta.internal.parsers.ScalametaParser.prefixExpr(ScalametaParser.scala:2058)
	scala.meta.internal.parsers.ScalametaParser.postfixExpr(ScalametaParser.scala:1924)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$expr$2(ScalametaParser.scala:1552)
	scala.meta.internal.parsers.ScalametaParser.atPosOpt(ScalametaParser.scala:327)
	scala.meta.internal.parsers.ScalametaParser.autoPosOpt(ScalametaParser.scala:370)
	scala.meta.internal.parsers.ScalametaParser.expr(ScalametaParser.scala:1480)
	scala.meta.internal.parsers.ScalametaParser.expr(ScalametaParser.scala:1381)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$funDefRest$1(ScalametaParser.scala:3547)
	scala.meta.internal.parsers.ScalametaParser.autoEndPos(ScalametaParser.scala:372)
	scala.meta.internal.parsers.ScalametaParser.autoEndPos(ScalametaParser.scala:377)
	scala.meta.internal.parsers.ScalametaParser.funDefRest(ScalametaParser.scala:3511)
	scala.meta.internal.parsers.ScalametaParser.funDefOrDclOrExtensionOrSecondaryCtor(ScalametaParser.scala:3460)
	scala.meta.internal.parsers.ScalametaParser.defOrDclOrSecondaryCtor(ScalametaParser.scala:3320)
	scala.meta.internal.parsers.ScalametaParser.nonLocalDefOrDcl(ScalametaParser.scala:3299)
	scala.meta.internal.parsers.ScalametaParser$$anonfun$templateStat$1.applyOrElse(ScalametaParser.scala:4150)
	scala.meta.internal.parsers.ScalametaParser$$anonfun$templateStat$1.applyOrElse(ScalametaParser.scala:4147)
	scala.PartialFunction.$anonfun$runWith$1$adapted(PartialFunction.scala:145)
	scala.meta.internal.parsers.ScalametaParser.statSeqBuf(ScalametaParser.scala:4107)
	scala.meta.internal.parsers.ScalametaParser.getStats$2(ScalametaParser.scala:4137)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$scala$meta$internal$parsers$ScalametaParser$$templateStatSeq$3(ScalametaParser.scala:4138)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$scala$meta$internal$parsers$ScalametaParser$$templateStatSeq$3$adapted(ScalametaParser.scala:4136)
	scala.meta.internal.parsers.ScalametaParser.scala$meta$internal$parsers$ScalametaParser$$listBy(ScalametaParser.scala:562)
	scala.meta.internal.parsers.ScalametaParser.scala$meta$internal$parsers$ScalametaParser$$templateStatSeq(ScalametaParser.scala:4136)
	scala.meta.internal.parsers.ScalametaParser.scala$meta$internal$parsers$ScalametaParser$$templateStatSeq(ScalametaParser.scala:4128)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$templateBody$1(ScalametaParser.scala:4006)
	scala.meta.internal.parsers.ScalametaParser.inBracesOr(ScalametaParser.scala:260)
	scala.meta.internal.parsers.ScalametaParser.inBraces(ScalametaParser.scala:256)
	scala.meta.internal.parsers.ScalametaParser.templateBody(ScalametaParser.scala:4006)
	scala.meta.internal.parsers.ScalametaParser.templateBodyOpt(ScalametaParser.scala:4009)
	scala.meta.internal.parsers.ScalametaParser.templateAfterExtends(ScalametaParser.scala:3960)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$templateOpt$1(ScalametaParser.scala:4001)
	scala.meta.internal.parsers.ScalametaParser.atPos(ScalametaParser.scala:325)
	scala.meta.internal.parsers.ScalametaParser.autoPos(ScalametaParser.scala:369)
	scala.meta.internal.parsers.ScalametaParser.templateOpt(ScalametaParser.scala:3993)
	scala.meta.internal.parsers.ScalametaParser.$anonfun$objectDef$1(ScalametaParser.scala:3722)
	scala.meta.internal.parsers.ScalametaParser.autoEndPos(ScalametaParser.scala:372)
	scala.meta.internal.parsers.ScalametaParser.autoEndPos(ScalametaParser.scala:377)
	scala.meta.internal.parsers.ScalametaParser.objectDef(ScalametaParser.scala:3714)
	scala.meta.internal.parsers.ScalametaParser.tmplDef(ScalametaParser.scala:3601)
	scala.meta.internal.parsers.ScalametaParser.topLevelTmplDef(ScalametaParser.scala:3589)
	scala.meta.internal.parsers.ScalametaParser$$anonfun$2.applyOrElse(ScalametaParser.scala:4121)
	scala.meta.internal.parsers.ScalametaParser$$anonfun$2.applyOrElse(ScalametaParser.scala:4115)
	scala.PartialFunction.$anonfun$runWith$1$adapted(PartialFunction.scala:145)
	scala.meta.internal.parsers.ScalametaParser.statSeqBuf(ScalametaParser.scala:4107)
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

file%3A%2F%2F%2FUsers%2Fseancoughlin%2Fprojects%2Fhiitbeats%2Fsrc%2Fbackend%2Fsrc%2Fmain%2Fscala%2FApiUtils.scala:105: error: illegal start of simple pattern
    val songs = findSongs(token)
    ^