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
    DefaultConnectionIdentifier.jndiName = "jdbc/yauserDS" 
    
    // add the connection manager if there's not already a JNDI connection defined
    if (!DB.jndiJdbcConnAvailable_?) DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendor)
    
    // where to search snippet
    LiftRules.addToPackages("com.opyate.yauser")

    Schemifier.schemify(true, Log.infoF _, User, YauserURL, Click)

    // Build SiteMap
//    val entries = Menu(Loc("Home", List("index"), "Home")) ::
//      Menu(Loc("addURL", List("addURL"), "Shorten a URL")) ::
//      Menu(Loc("u", List("u"), "Retrieve a URL", Hidden)) ::
//      Menu(Loc("new_acct", List("new_acct"), "New Account", Hidden)) ::
//      Menu(Loc("404", List("404"), "404", Hidden)) ::
//      Nil
//    LiftRules.setSiteMap(SiteMap(entries:_*))
    
    // dispatch
    LiftRules.dispatch.prepend {
      case r @ Req("u" :: id :: Nil, "", GetRequest) => () => Yurl.clickU(id)
      case r @ Req("i" :: id :: Nil, "", GetRequest) => () => Yurl.clickI(id)
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
//      Class.forName("org.apache.derby.jdbc.EmbeddedDriver")
//      val dm = DriverManager.getConnection("jdbc:derby:yauser;create=true")
      Class.forName("com.mysql.jdbc.Driver")
      val dm = DriverManager.getConnection("jdbc:mysql://localhost:3306/yauser?createDatabaseIfNotExist=true&user=root")
      
      Full(dm)
    } catch {
      case e : Exception => e.printStackTrace; Empty
    }
  }
  def releaseConnection(conn: Connection) {conn.close}
}


