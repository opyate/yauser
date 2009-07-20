package com.opyate.yauser.snippet

import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.util._
import com.opyate.yauser.model._
import net.liftweb.http.S 
import scala.xml._

abstract class OURL {
  
}

object OURL {
  def retrieveURL(req: Req): Box[LiftResponse] = {
    println("Inside OURL: " + req.toString)
    S.redirectTo("http://localhost:8080")
  }
}