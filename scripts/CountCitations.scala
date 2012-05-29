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
  "jerz2007"      ->   9, "eve2007"       ->  10, "barnet2008"    ->  15,
  "blackwell2009" ->  35, "brown2009"     ->  40, "flanders2009"  ->  55,
  "svensson2009"  ->  65, "svensson2009a" ->  65, "dunn2009"      ->  79,
  "svensson2010"  ->  80, "kashtan2011"   -> 101
).mapValues("dhq-%06d".format(_)) withDefault identity

val BadCitation = "^#(example|fig(?:ure)?|glossary|appendix|note|section|table)_?[\\d\\.]*".r
val GoodCitation = "^#(.*)$".r

def isValid(url: String) = url match {
  case BadCitation(_) => None
  case GoodCitation(id) => Some(id)
  case _ => None
}

class RichNode(e: Node) {
  private val xmlns = "http://www.w3.org/XML/1998/namespace"
  def xmlId = e.attribute(xmlns, "id").map(_.text)
  def hasAttr(k: String, v: String) =
    e.attribute(k).flatMap(_.headOption).map(_.text == v).getOrElse(false)
}

implicit def enrichNode(e: Node) = new RichNode(e)

new File(args(0)).listFiles.sorted.view.map(XML.loadFile).flatMap { doc =>
  val id = "dhq-" +
    (doc \\ "idno").find(_.hasAttr("type", "DHQarticle-id")).get.text

  val bibed = (doc \\ "bibl").flatMap(_.xmlId.map(overlap)).toSet

  val cited = (doc \\ "ptr" \\ "@target" ++ doc \\ "ref" \\ "@target").flatMap(
    v => isValid(v.text)
  ).map(overlap).groupBy(identity).mapValues(_.size)

  // Items that are in the bibliography but not cited.
  val uncited = (bibed -- cited.keys)

  // Items cited but not in the bibliography.
  val unbibed = (cited.keys.toSet -- bibed)

  if (unbibed.nonEmpty) System.err.println(
    "--------------- %s\nText only: %s".format(
      id, unbibed.mkString(", ")
    )
  )

  val counts = cited ++ uncited.map((_, 0)).toMap
  counts.map { case (target, count) => (id, target, count) }

}.map(_.productIterator.mkString(",")).foreach(println)

