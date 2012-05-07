// Usage: scala scripts/CountCitations.scala data/text/ > data/other/links.txt
// The output is a CSV file with each line being of the following format:
//
//     sourceId,targetId,count
//
// Note that often the count is zero, for items that are included in the
// bibliography but not cited in the text.

import java.io.File
import scala.xml._

val xmlns = "http://www.w3.org/XML/1998/namespace"

val docs = new File(args(0)).listFiles.sorted.view.map(XML.loadFile)
val cites = docs.flatMap { doc =>
  val id = (doc \\ "idno").filter(
    _.attribute("type").map(_.head.toString).getOrElse("") == "DHQarticle-id"
  ).head.text

  val ids = (doc \\ "bibl").flatMap(
    _.attribute(xmlns, "id").map(_.head.toString)
  ).toSet

  val refTargets = (doc \\ "ptr" ++ doc \\ "ref").flatMap(
    _.attribute("target").map(_.head.toString)
  ).filter(_.head == '#').map(_.tail).groupBy(identity).mapValues(_.size)

  val unseens = (ids -- refTargets.keys).map(k => (k, 0)).toMap

  val counts = refTargets ++ unseens
  counts.map { case (target, count) => (id, target, count) }
}.foreach(i => println(i.productIterator.mkString(",")))

