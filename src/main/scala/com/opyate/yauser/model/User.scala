package com.opyate.yauser.model


import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util.Helpers._
import _root_.java.util.regex._

/**
 * The singleton that has methods for accessing the database
 */
object User extends User with KeyedMetaMapper[Long, User] {
  override def dbTableName = "users" // define the DB table name

  // define the order fields will appear in forms and output
  override def fieldOrder = id :: name :: firstName :: lastName :: email ::  password :: Nil

  val validName = Pattern.compile("^[a-z0-9_]{3,30}$")
}

/**
 * An O-R mapped "User" class that includes first name, last name, password and we add a "Personal Essay" to it
 */
class User extends ProtoUser[User] {
  def getSingleton = User // what's the "meta" server

  def wholeName = firstName+" "+lastName

  // The Name of the User
  object name extends MappedString(this, 32) {
    // input filter for the user name
    override def setFilter = notNull _ :: toLower _ :: trim _ :: super.setFilter

    // validation for the user name
    override def validations = valMinLen(3, "Name too short") _ ::
     valRegex(User.validName, "The 'name' must be letters, numbers, or the '_' (underscore)") _ ::
     valUnique("The name '"+is+"' is already taken") _ ::
     super.validations

    override def dbIndexed_? = true
  }
}