package com.opyate.yauser.snippet

import com.opyate.yauser.model.YauserURL
import scala.xml.NodeSeq
import net.liftweb.http.S._
import net.liftweb.http.S  
import net.liftweb.http.SHtml._
import net.liftweb.http.RequestVar
import net.liftweb.util.Helpers._
import net.liftweb.util.Full
import java.net.{URL,MalformedURLException}

/**
 * @author Juan Uys <opyate@gmail.com>
 * 
 * Snippet that deals with URL-related actions (showing/saving/etc)
 */
class YURL {
  object yx extends RequestVar(Full("")) // default is empty string
  
  def show(xhtml: NodeSeq): NodeSeq = {
    def doNothing() = {}
    
    val urlSent = !(yx.isEmpty || yx.open_!.length == 0)
    val yurl:YauserURL = YauserURL.create
    
    if (urlSent) {
      // first check URL validity
      try {
        new URL(yx.open_!)
        
        // save the new url
        yurl.originalURL(yx.open_!).save
      } catch {
        case ex: MalformedURLException => S.error("Bad URL")
      }
    }
    
    val message: String =
      if (yurl.saved_?)
        "You've successfully added the URL."
      else
        "Please add a valid URL."
    
    val newYURL: URL = new URL("http://" + S.hostName + "/u/" + yurl.urlId)
    
    bind("y", xhtml,
        "addURL" --> text("", v => yx(Full(v))) % ("size" -> "10") % ("id" -> "addURL"),
        "response" --> message,
        "newYURL" --> newYURL.toString,
        "submit" --> submit("AddURL", doNothing)
      )
  }
}
