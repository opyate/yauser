package com.opyate.yauser.model

import net.liftweb._ 
import mapper._ 
import http._ 
import SHtml._ 
import util._ 
import Helpers._
 
import _root_.java.util.Date
 
class Click extends LongKeyedMapper[Click] with IdPK with TimeStamp[Click] 
  with Ordered[Click] with OneToMany[Long, Click] {
  def getSingleton = Click
  
  object yauserurl extends MappedLongForeignKey(this, YauserURL)
  
  object sort extends MappedInt(this)    

  def compare(that: Click) = this.sort.is - that.sort.is
  
  object referer extends MappedString(this, 32) {
    override def defaultValue = S.request.open_!.remoteAddr
  }
}

object Click extends Click with LongKeyedMetaMapper[Click] {
  override def dbTableName = "clicks" // define the DB table name
}

/**
 * TimeStamp trait from
 * http://groups.google.com/group/liftweb/browse_thread/thread/ae1c9be8a6fc1997/fa225133f60e81e4?lnk=gst&q=timestamp#fa225133f60e81e4
 */
trait TimeStamp[MapperType <: TimeStamp[MapperType]] extends Mapper [MapperType] { 
  self:MapperType => 
  object xdatetime extends MappedDateTime(this) 
  // all sorts of utility functions for dealing with timestamps 
} 