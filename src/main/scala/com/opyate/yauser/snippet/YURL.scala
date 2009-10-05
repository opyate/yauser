package com.opyate.yauser.snippet

import com.opyate.yauser.model._
import scala.xml.NodeSeq
import net.liftweb.http.S._
import net.liftweb.http.S  
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
          "addURL" --> text("", v => yx(Full(v))) % ("size" -> "10") % ("id" -> "addURL"),
          "response" --> "You didn't submit anything",
          "newYURL" --> "..." ,
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
	      yurl.originalURL(newURL).addedBy(u).save
	      println("Was it really saved: " + yurl.saved_?)
	 
	      val message: String =
	      if (yurl.saved_?)
	        "URL saved."
	      else
	        "URL was not saved..."
	 
	      // TODO handle port numbers
	      val newYURL: URL = new URL("http://" + S.hostName + "/u/" + yurl.urlId)
	      
	      bind("y", xhtml,
	          "addURL" --> text("", v => yx(Full(v))) % ("size" -> "10") % ("id" -> "addURL"),
	          "response" --> message,
	          "newYURL" --> { if (yurl.saved_?) newYURL.toString else "" },
	          "submit" --> submit("AddURL", doNothing)
	        )
	    }
    }
  }
  
  def show_last_added_urls(xhtml: Group): NodeSeq = {
    if (logged_in_?) {
      Helpers.bind("sk", xhtml,
          "originalURL" -> <span>Latest URLs:<ul>{
            YauserURL.findAll(
              By(YauserURL.addedBy, User.find(By(User.name, S.get("user_name").open_!))),
              StartAt(0),
              MaxRows(10)
            ) 
            .flatMap {
              u =>
              <li>{u.originalURL}</li>
            }
          }</ul></span>)
    } else {
      <span>Log in to see your URLs</span>
    }
  }
  
  def cur_name:  MetaData = new UnprefixedAttribute("name", Text(S.param("user").openOr("")), Null)

  def logged_in_? = S.get("user_name").isDefined
}
