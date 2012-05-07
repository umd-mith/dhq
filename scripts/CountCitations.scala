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

val overlap = Map(
  "jerz2007" -> 9,
  "eve2007" -> 10,
  "barnet2008" -> 15,
  "blackwell2009" -> 35,
  "brown2009" -> 40,
  "flanders2009" -> 55,
  "svensson2009" -> 65,
  "svensson2009a" -> 65,
  "dunn2009" -> 79,
  "svensson2010" -> 80,
  "kashtan2011" -> 101
).mapValues("dhq-%06d".format(_)).withDefault(identity)

val xmlns = "http://www.w3.org/XML/1998/namespace"

val BadCitation = "^#(example|fig(?:ure)?|glossary|note|section|table)_?\\d.*".r
val GoodCitation = "^#(.*)$".r

def isValid(url: String) = url match {
  case BadCitation(_) => None
  case GoodCitation(id) => Some(id)
  case _ => None
}

val docs = new File(args(0)).listFiles.sorted.view.map(XML.loadFile)
val cites = docs.flatMap { doc =>
  val id = "dhq-" + (doc \\ "idno").filter(
    _.attribute("type").map(_.head.toString).getOrElse("") == "DHQarticle-id"
  ).head.text

  val bibed = (doc \\ "bibl").flatMap(
    _.attribute(xmlns, "id").map(id => overlap(id.head.toString))
  ).toSet

  val cited = (doc \\ "ptr" ++ doc \\ "ref").flatMap(
    _.attribute("target").map(_.head.toString)
  ).flatMap(isValid).map(overlap).groupBy(identity).mapValues(_.size)

  // Items that are in the bibliography but not cited.
  val uncited = (bibed -- cited.keys)

  // Items cited but not in the bibliography.
  val unbibed = (cited.keys.toSet -- bibed)

  if (uncited.nonEmpty || unbibed.nonEmpty) System.err.println(
    "--------------- %s\nBibl only: %s\nText only: %s".format(
      id, uncited.mkString(", "), unbibed.mkString(", ")
    )
  )

  val counts = cited ++ uncited.map((_, 0)).toMap

  counts.map { case (target, count) => (id, target, count) }

}.map(_.productIterator.mkString(",")).foreach(println)

