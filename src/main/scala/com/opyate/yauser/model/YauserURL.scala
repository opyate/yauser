package com.opyate.yauser.model

import net.liftweb._ 
import mapper._ 
import http._ 
import SHtml._ 
import util._ 
 
class YauserURL extends LongKeyedMapper[YauserURL] with IdPK { 
  def getSingleton = YauserURL
  object originalURL extends MappedPoliteString(this, 4096)
} 
 
object YauserURL extends YauserURL with LongKeyedMetaMapper[YauserURL]