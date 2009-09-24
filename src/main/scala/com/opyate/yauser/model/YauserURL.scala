package com.opyate.yauser.model

import net.liftweb._ 
import mapper._ 
import http._ 
import SHtml._ 
import util._ 
import Helpers._
 
import _root_.java.util.Date
 
class YauserURL extends KeyedMapper[String, YauserURL] {
  def getSingleton = YauserURL
  /* MAC address as primary key */
  def primaryKeyField = urlId
  object urlId extends MappedStringIndex(this, 17) with IndexedField[String] {
    override def writePermission_? = true
    override def dbDisplay_? = true
    //override def dbAutogenerated_? = false
    //override lazy val defaultValue = randomString(maxLen)
      println("setting default value...")
    override lazy val defaultValue = ""
 
    private var myDirty = false
    override def dirty_? = myDirty
    override def dirty_?(b : Boolean) = { myDirty = b; super.dirty_?(b) }
    override def fieldCreatorString(dbType: DriverType, colName: String): String = colName+" CHAR("+maxLen+") NOT NULL "
  }
  // object name extends MappedPoliteString(this, 128)
  //object x extends MappedInt(this)
  //object ts extends MappedDateTime(this) with MappedTimestamp[MappedDateTime[Date]]
  object originalURL extends MappedPoliteString(this, 4096)
}
 
object YauserURL extends YauserURL with KeyedMetaMapper[String, YauserURL] {
 override def dbTableName = "yauserurl" // define the DB table name
}