package bootstrap.liftweb

import _root_.net.liftweb.util._
import _root_.net.liftweb.http._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import Helpers._
import com.opyate.yauser.snippet._

import _root_.net.liftweb.mapper.{DB, ConnectionManager, Schemifier, 
   DefaultConnectionIdentifier, ConnectionIdentifier,By}
import java.sql.{Connection, DriverManager}
import com.opyate.yauser.model._


/**
  * A class that's instantiated early and run.  It allows the application
  * to modify lift's environment
  */
class Boot {
  def boot {
    // The default 'lift' JNDI name can be changed here:
    DefaultConnectionIdentifier.jndiName = "jdbc/lift" 
    
    // add the connection manager if there's not already a JNDI connection defined
    if (DB.jndiJdbcConnAvailable_?) DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendor)
    
    // where to search snippet
    LiftRules.addToPackages("com.opyate.yauser")
    //Schemifier.schemify(true, Log.infoF _, User)
    Schemifier.schemify(true, Log.infoF _, YauserURL)

    // Build SiteMap
    val entries = Menu(Loc("Home", List("index"), "Home")) ::
      Menu(Loc("addURL", List("addURL"), "Shorten a URL")) ::
      Menu(Loc("u", List("u"), "Retrieve a URL", Hidden)) ::
      Menu(Loc("404", List("404"), "404", Hidden)) ::
      Nil
    LiftRules.setSiteMap(SiteMap(entries:_*))
    //S.addAround(User.requestLoans)
    
    // dispatch
    LiftRules.dispatch.prepend {
      case r @ Req("u" :: id :: Nil, "", GetRequest) => () =>
        val yurl = YauserURL.find(By(YauserURL.id, id.toLong))
        println(yurl)
        println(yurl.getClass)
        if (yurl.isEmpty)
          Full(RedirectResponse("/404"))
        else
          Full(RedirectResponse(yurl.elements.next.originalURL));
    }
    
    // Redirects
	LiftRules.uriNotFound.prepend{
	  case (req, _) => PermRedirectResponse("404", req)
	}
  }
}

object DBVendor extends ConnectionManager {
  def newConnection(name: ConnectionIdentifier): Box[Connection] = {
    try {
      Class.forName("org.apache.derby.jdbc.EmbeddedDriver")
      val dm = DriverManager.getConnection("jdbc:derby:yauser;create=true")
      Full(dm)
    } catch {
      case e : Exception => e.printStackTrace; Empty
    }
  }
  def releaseConnection(conn: Connection) {conn.close}
}


