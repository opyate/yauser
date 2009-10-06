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

/**
 * @author Juan Uys <opyate@gmail.com>
 * 
 * Snippet that deals with URL-related actions (showing/saving/etc)
 */
class Yurl {
  object yx extends RequestVar(Full("")) // default is empty string
  
  def main(xhtml: NodeSeq): NodeSeq = {
    if (!logged_in_?) {S.error("Log in before adding a new URL."); S.redirectTo("/")}
    else {
      def doNothing() = {}
    
      if (yx.isEmpty || yx.open_!.length == 0) {
        
        bind("y", xhtml,
          "addURL" --> text("", v => yx(Full(v))) % ("size" -> "50") % ("id" -> "addURL"),
          "response" --> "",
          "u_URL" --> "" ,
          "i_URL" --> "" ,
          "submit" --> submit("AddURL", doNothing)
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
        println("Saving new URL: " + newURL + " with ID: " + yurl.urlId)
	      // Cat.find("foo") openOr Cat.create.mac("foo")
        val u = User.find(By(User.name, S.get("user_name").open_!))
        Log.info("User currently logged in : " + u.open_!.email)
	    yurl.originalURL(newURL).addedBy(u).xdatetime(new _root_.java.util.Date).save
	    println("Was it really saved: " + yurl.saved_?)

	    val message: String =
	    if (yurl.saved_?)
	      "URL saved."
        else
	      "URL was not saved..."

	    // TODO handle port numbers
	    val u_URL: URL = new URL("http://" + S.hostName + "/u/" + yurl.urlId)
        val i_URL: URL = new URL("http://" + S.hostName + "/i/" + yurl.id)

	    bind("y", xhtml,
	        "addURL" --> text("", v => yx(Full(v))) % ("size" -> "50") % ("id" -> "addURL"),
	        "response" --> message,
	        "u_url" --> { if (yurl.saved_?) "URL by token: " + u_URL.toString else "" },
            "i_url" --> { if (yurl.saved_?) "URL by ID: " + i_URL.toString else "" },
	        "submit" --> submit("AddURL", doNothing)
	      )
	  }
    }
  }
  
  def show_last_added_urls(xhtml: Group): NodeSeq = {
    if (logged_in_?) {
      Helpers.bind("sk", xhtml,
          "username" -> S.get("user_name").open_!,
          "content" ->
            <table>
              <caption>URLs</caption>
              <thead>
              <tr>
                <th>ID</th>
                <th>TOKEN</th>
                <th>URL</th>
                <th>Stats</th>
              </tr>
              </thead>
              <tbody>
            {
            YauserURL.findAll(
              By(YauserURL.addedBy, User.find(By(User.name, S.get("user_name").open_!))),
              StartAt(0),
              MaxRows(10)
            )
            .flatMap {
              u =>
              <tr>
                <td>{u.id}</td>
                <td>{u.urlId}</td>
                <td>{u.originalURL}</td>
                <td><a href={"/stats?id=" + u.id}>stats</a></td>
              </tr>
            }
          }
            </tbody>
          </table>)
    } else {
      Helpers.bind("sk", xhtml,
          "username" -> "Not logged in.",
          "content" -> <span>Please log in to continue...</span>)
    }
  }
  
  def stats(xhtml: Group): NodeSeq = {
    if (logged_in_?) {
      val yurl = YauserURL.findAll(
        By(YauserURL.id, S.param("id").open_!.toLong)
      ).head
      
      println("Reading stats for URL: " + yurl.id)
      
//      val clicks = Click.findAll(
//        By(Click.yauserurl, yurl.id)
//      )
      
      // I don't know if there's support for Count, and GroupBy yet.
      // This returns (List[String], List[List[String]])
      // e.g.
      // (List("someString", "someInt"]),
      // List(
      //      List("str1","1"),
      //      List("str2","2"),
      //      List("str3","3"))
      // )
      val resultSet = DB.runQuery(
        "select referer, count(referer) as cnt " +
        " from clicks where yauserurl = ? group by referer order by cnt",
        List(yurl.id.toLong))
      
      Helpers.bind("sk", xhtml,
          "username" -> S.get("user_name").open_!,
          "content" ->
            <table>
              <caption>URL</caption>
              <thead>
                <tr>
              	  <th>ID</th>
                  <th>TOKEN</th>
                  <th>URL</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>{yurl.id}</td>
                  <td>{yurl.urlId}</td>
                  <td>{yurl.originalURL}</td>
                </tr>
              </tbody>
            </table>
            
            <br/>
            
            <table>
              <caption>Stats</caption>
              <thead>
                <tr>
              	  <th>Referer</th>
                  <th>Clicks</th>
                </tr>
              </thead>
              <tbody>
              <tr>
              {
                // we know what we queried, thus are only interested in _2 (_1 are the column names)
                for (
                  val rows <- resultSet._2
                ) yield {
                  for (
                    val row <- rows
                  ) yield {
                    <td>{row}</td>
                  }
                }
              }
              </tr>
              </tbody>
            </table>
          )
    } else {
      Helpers.bind("sk", xhtml,
          "username" -> "Not logged in.",
          "content" -> <span>Please log in to continue...</span>)
    }
  }
  
  def cur_name:  MetaData = new UnprefixedAttribute("name", Text(S.param("user").openOr("")), Null)

  def logged_in_? = S.get("user_name").isDefined
  
  
}

object Yurl {
  private def click(yurl: Box[YauserURL]): Full[RedirectResponse] = {
    if (yurl.isEmpty)
      Full(RedirectResponse("/404"))
    else {
      // save metrics, then redirect
      println("referer: " + S.request.open_!.remoteAddr)
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
