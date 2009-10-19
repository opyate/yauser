package com.opyate.yauser.snippet

import com.opyate.yauser.model._
import scala.xml.NodeSeq
import net.liftweb.http._
import net.liftweb.http.S._  
import net.liftweb.http.SHtml._
import net.liftweb.http.RequestVar
import net.liftweb.util._
import net.liftweb.util.Helpers._
import net.liftweb.util.Full
import net.liftweb.http.js.JsCmds.{Alert, Noop}
import java.net.{URL,MalformedURLException}
import _root_.scala.xml._
import _root_.net.liftweb.mapper._
import view._

/**
 * @author Juan Uys <opyate@gmail.com>
 * 
 * Snippet that deals with URL-related actions (showing/saving/etc)
 * Some ideas from:
 * http://www.ibm.com/developerworks/opensource/library/os-ag-lift/
 */
class Yurl {
  
  val pageLimit = 25 // global pagination size
  object pageNumber extends RequestVar[Int](S.param("p").map(_.toInt) openOr 0)
  object yx extends RequestVar(Full("")) // default is empty string
  
  /**
   * This method handles the addition of a new URL.
   */
  def main(xhtml: NodeSeq): NodeSeq = {
    if (!logged_in_?) {
      S.error("Log in before adding a new URL.")
      S.redirectTo("/")
    } else {
      def doNothing() = {}
    
      if (yx.isEmpty || yx.open_!.length == 0) {
        
        bind("y", xhtml,
          "addURL" -> text("", v => yx(Full(v))) % ("size" -> "50") % ("id" -> "addURL"),
          "response" -> "",
          "u_url" -> "" ,
          "i_url" -> "" ,
          "submit" -> submit("Shorten!", doNothing)
        )
      } else {
        val newURL = yx.open_!
        Log.info("URL submitted: " + newURL)
        // first check URL validity
        try {
          new URL(newURL)
        } catch {
          case ex: MalformedURLException => S.error("Bad URL")
          case unknown => S.error("Unknown error: " + unknown)
        }
      
        // save the new url
        val yurl: YauserURL = YauserURL.create
        
        val u = User.find(By(User.name, S.get("user_name").open_!))

        try {
	      yurl.originalURL(newURL).addedBy(u).xdatetime(new _root_.java.util.Date).save
        } catch {
          case e: com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException => {S.error("This URL already exists.")}
          case u => {S.error("Unknown error: " + u.getMessage)}
        }

	    val message: String =
	    if (yurl.saved_?) {
	      Log.info("Saved new URL: " + newURL + ", id=" + yurl.id + ", token=" + yurl.urlId)
	      "URL saved."
	    } else
	      "URL was not saved..."

	    // TODO handle port numbers
	    val u_URL: URL = new URL("http://" + S.hostName + "/u/" + yurl.urlId)
        val i_URL: URL = new URL("http://" + S.hostName + "/i/" + yurl.id)

	    bind("y", xhtml,
	        "addURL" -> text("", v => yx(Full(v))) % ("size" -> "50") % ("id" -> "addURL"),
	        "response" -> message,
	        "u_url" -> { if (yurl.saved_?) "URL by token: " + u_URL.toString else "" },
            "i_url" -> { if (yurl.saved_?) "URL by ID: " + i_URL.toString else "" },
	        "submit" -> submit("AddURL", doNothing)
	      )
	  }
    }
  }
  
  /**
   * Shows the last few URLs added by the currently logged-in user.
   */
  def show_last_added_urls(xhtml: Group): NodeSeq = {
    if (logged_in_?) {
      val all = YauserURL.findAll(
        By(YauserURL.addedBy, User.find(By(User.name, S.get("user_name").open_!))),
        StartAt(pageNumber.is * pageLimit),
        MaxRows(pageLimit)
      )
      
      bind("y", xhtml,
        "header" -> Text(S.get("user_name").open_! + "'s URLs"),
        "content" -> bind("y", chooseTemplate("main","content", xhtml),
          "next_link" -> <a href={"/?p=" + (pageNumber.is + 1)}>next</a>,
          "previous_link" -> <a href={"/?p=" + (if (pageNumber.is > 0) pageNumber.is - 1 else 0)}>previous</a>,
          "urls" -> all.flatMap { u => 
            bind("u", chooseTemplate("url","entries", xhtml),
              "id" -> u.id,
              "url_id" -> u.urlId,
              "original_url" -> u.originalURL,
              "xdatetime" -> u.xdatetime,
              "stats_link" -> <a href={"/stats?id=" + u.id + "&p=0"}>stats</a>
            )
          }
        )
      )
    } else {
      bind("y", xhtml,
        "header" -> "Not logged in.",
        "content" -> "Please log in to see your content."
      )
    }
  }
  
  /**
   * Shows the stats for a specified URL.
   */
  def stats(xhtml: Group): NodeSeq = {
    if (logged_in_?) {
      val yurl = YauserURL.findAll(
        By(YauserURL.id, S.param("id").open_!.toLong)
      ).head
      
      Log.debug("Reading stats for URL: " + yurl.id)
      
      // per-referrer stats, paginated
      val clicksPerReferrer = DB.runQuery(
        "select referrer, count(referrer) as cnt " +
        " from clicks where yauserurl = ? group by referrer order by cnt desc limit " +
          (pageNumber.is * pageLimit) + ", " + pageLimit,
        List(yurl.id.toLong))
      
      val clicksPerReferrerCount: NodeSeq = for (
                  val rows <- clicksPerReferrer._2
                ) yield {
                  <tr>
                  {
                    for (
                      val row <- rows
                    ) yield {
                      <td>{row}</td>
                    }
                  }
                  </tr>
                }
      
      // count of all clicks
      val allClicks = DB.runQuery(
        "select count(referrer) as cnt " +
        " from clicks where yauserurl = ? order by cnt",
        List(yurl.id.toLong))
      
      val allClicksCount: NodeSeq = for (
                  val rows <- allClicks._2
                ) yield {
                  <span>
                  {
                    for (
                      val row <- rows
                    ) yield {
                      {row}
                    }
                  }
                  </span>
                }
      
      // count of unique referrers
      val uniqueReferrers = DB.runQuery(
        "select count(distinct(referrer)) as cnt " +
        " from clicks where yauserurl = ?",
        List(yurl.id.toLong))

//      val uniqueReferrersCount: Int = for (val rows <- uniqueReferrers._2) yield {{
//        for (val row <- rows) yield { row }}}
      val uniqueReferrersCount: NodeSeq = for (
                  val rows <- uniqueReferrers._2
                ) yield {
                  <span>
                  {
                    for (
                      val row <- rows
                    ) yield {
                      {row}
                    }
                  }
                  </span>
                }
      
      bind("y", xhtml,
        "header" -> Text("Stats for " + yurl.urlId),
        "content" -> bind("y", chooseTemplate("main","content", xhtml),
          "next_link" -> <a href={"/stats?id=" + yurl.id + "&p=" + (pageNumber.is + 1)}>next</a>,
          "previous_link" -> <a href={"/stats?id=" + yurl.id + "&p=" + (if (pageNumber.is > 0) pageNumber.is - 1 else 0)}>previous</a>,
          "id" -> yurl.id,
          "url_id" -> yurl.urlId,
          "original_url" -> <a href={""+yurl.originalURL}>{yurl.originalURL}</a>,
          "date_added" -> yurl.xdatetime,
          "all_clicks" -> allClicksCount,
          "unique_clicks" -> uniqueReferrersCount,
          "stats" -> clicksPerReferrerCount
        )
      )
    } else {
      bind("y", xhtml,
        "header" -> "Not logged in.",
        "content" -> "Please log in to see your content."
      )
    }
  }
  
  def cur_name:  MetaData = new UnprefixedAttribute("name", Text(S.param("user").openOr("")), Null)

  def logged_in_? = S.get("user_name").isDefined
  
  
}

object Yurl {
  object pageSize extends RequestVar[Long](S.param("pageSize").map(_.toLong) openOr 25L)
  
  /**
   * Click-through counter.
   */
  private def click(yurl: Box[YauserURL]): Full[RedirectResponse] = {
    if (yurl.isEmpty)
      Full(RedirectResponse("/404"))
    else {
      // save metrics, then redirect
      Log.info("Click tracker: token: " + yurl.open_!.urlId + ", referrer: " + S.request.open_!.remoteAddr)
      Click.create.yauserurl(yurl).xdatetime(new _root_.java.util.Date).save
      Full(RedirectResponse(yurl.open_!.originalURL));
    }
  }
  
  def clickU(id: String): Full[RedirectResponse] = {
    val yurl = YauserURL.find(By(YauserURL.urlId, id))
    click(yurl)
  }
  
  def clickI(id: String): Full[RedirectResponse] = {
    val yurl = YauserURL.find(By(YauserURL.id, id.toLong))
    click(yurl)
  }
  
}
