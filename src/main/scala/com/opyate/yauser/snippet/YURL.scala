package com.opyate.yauser.snippet

import com.opyate._ 
import model._ 
 
import net.liftweb._ 
import http._ 
import SHtml._ 
import S._ 
 
import js._ 
import JsCmds._ 
 
import mapper._ 
 
import util._ 
import Helpers._ 
 
import scala.xml.{NodeSeq, Text} 
 
class YURL {

 def add(form: NodeSeq) = { 
   val yauserURL = YauserURL.create
 
   def checkAndSave(): Unit = 
   yauserURL.validate match { 
    case Nil => yauserURL.save ; S.notice("Added "+yauserURL.originalURL) 
    case xs => S.error(xs) ; S.mapSnippet("YURL.add", doBind) 
   } 
 
   def doBind(form: NodeSeq) = 
   bind("yauserURL", form,
      "originalURL" -> yauserURL.originalURL.toForm, 
      "submit" -> submit("New", checkAndSave)) 
 
   doBind(form) 
 } 
}