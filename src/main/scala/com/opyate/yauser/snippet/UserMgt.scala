package com.opyate.yauser.snippet

import _root_.scala.xml._
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.S._
import _root_.net.liftweb.http.SHtml._
import _root_.com.opyate.yauser.model._
import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util.Helpers._
import _root_.net.liftweb.util._

class UserMgt {
  def login_panel(xhtml: Group): NodeSeq = {
    <br/> ++
    (if (logged_in_?) {
      <div class="tabs">
        <ul>
          <li><a href="/logout">Log out</a></li>
          <li><a href="/addURL">Add URL</a></li>
          <li><a href="/">Home</a></li>
        </ul>
      </div>
    } else {
        var username = ""
        var pwd = ""
        def testPwd {
           User.find(By(User.name, username)).filter(_.password.match_?(pwd)).map{
             u => S.set("user_name", u.name)
             Log.info("Logged in: " + u.email)
             S.redirectTo("/")
           }.openOr(S.error("Invalid Username/Password"))
        }
      <form method="post" action={S.uri}>
      <table>
      <tr><td>Username:</td><td>{text("", username=_)}</td></tr>
      <tr><td>Password:</td><td>{password("", pwd=_)}</td></tr>
      <tr>
        <td>&nbsp;</td>
        <td>{submit("login", testPwd _)}</td></tr>
      </table>
      </form>
    })
  }

  def new_account: NodeSeq = {
    if (logged_in_?) {S.error("Can't create new account if you're logged in"); S.redirectTo("/")}
    else {
      val invokedAs = S.invokedAs
      val theUser = new User
      def newAccount(ignore: NodeSeq): NodeSeq = {
        def saveMe(in: User) {
          // validate the user
          val issues = theUser.validate

          // if there are no issues, set the friendly name, destroy the token, log the user in and send them to the home page
          if (issues.isEmpty) {
            theUser.save
            S.set("user_name", theUser.name)
            S.notice("Welcome to Yauser!")
            Log.info("New account created: " + theUser.email)
            redirectTo("/")
          }

          // This method tells lift that if we get another call to the same snippet during a page
          // reload, don't create a new instance, just invoke the innerAccept function which has
          // "user" bound to it
          S.mapSnippet(invokedAs, newAccount)

          // whoops... we have issues, display them to the user and continue loading this page
          error(issues)
        }
        <form method="post" action={S.uri}>
        <table>{
          theUser.toForm(Empty, saveMe _)
        }
        <tr><td>&nbsp;</td><td><input type="submit" value="Create New Account"/></td></tr></table>
        </form>
      }

      newAccount(Text(""))
    }
  }

  def logout: NodeSeq = {
    S.unset("user_name")
    S.redirectTo("/")
  }

  def cur_name:  MetaData = new UnprefixedAttribute("name", Text(S.param("user").openOr("")), Null)

  def logged_in_? = S.get("user_name").isDefined
}
