package com.opyate.yauser.snippet

import com.opyate.yauser.model.YauserURL
import scala.xml.NodeSeq
import net.liftweb.http.S._
import net.liftweb.http.S  
import net.liftweb.http.SHtml._
import net.liftweb.http.RequestVar
import net.liftweb.util.Helpers._
import net.liftweb.util.Full
import net.liftweb.http.js.JsCmds.{Alert, Noop}
import java.net.{URL,MalformedURLException}
import _root_.scala.xml._

/**
 * @author Juan Uys <opyate@gmail.com>
 * 
 * Snippet that deals with URL-related actions (showing/saving/etc)
 */
class YURL {
  object yx extends RequestVar(Full("")) // default is empty string
  
  def main(xhtml: NodeSeq): NodeSeq = {
    if (!logged_in_?) {S.error("Log in before adding a new URL."); S.redirectTo("/")}
    else {
      val invokedAs = S.invokedAs
      println("Invoked by: " + invokedAs)
      def doNothing() = {}
    
      if (yx.isEmpty) {
        // user didn't submit anything
        bind("y", xhtml,
          "addURL" --> text("", v => yx(Full(v))) % ("size" -> "10") % ("id" -> "addURL"),
          "response" --> "You didn't submit anything",
          "newYURL" --> "..." ,
          "submit" --> submit("AddURL", doNothing)
        )
      } else {
        val newURL = yx.open_!
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
	      yurl.originalURL(newURL).save
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
  
  def cur_name:  MetaData = new UnprefixedAttribute("name", Text(S.param("user").openOr("")), Null)

  def logged_in_? = S.get("user_name").isDefined
}
