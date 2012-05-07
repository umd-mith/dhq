// Usage:
//     scala scripts/CountCitations.scala data/text/ \
//      1> data/other/links.txt \
//      2> log/errors.txt
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

  // Items that are in the bibliography but not cited.
  val uncited = (ids -- refTargets.keys)

  // Items cited but not in the bibliography.
  val unbibed = (refTargets.keys.toSet -- ids)

  if (uncited.nonEmpty || unbibed.nonEmpty) System.err.println(
    "--------------- %s\nBibl only: %s\nText only: %s".format(
      id, uncited.mkString(", "), unbibed.mkString(", ")
    )
  )

  val counts = refTargets ++ uncited.map((_, 0)).toMap

  counts.map { case (target, count) => (id, target, count) }

}.foreach(i => println(i.productIterator.mkString(",")))

