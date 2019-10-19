package models.tableDefinitions

import models.Session
import models.SessionType.SessionToken
import models.UsersId.UserId
import slick.model.ForeignKeyAction
//import slick.driver.MySQLDriver.api._
import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag




class SessionTableDef(tag : Tag)   extends BaseTable[Session](tag, "sessions")  with TableId[SessionToken] {

  override def id   = column[String]("token", O.Unique , O.PrimaryKey )

  def userId : Rep[UserId] = column [UserId]("user_id")
  def starts = column[java.sql.Timestamp]("starts")
  def maxInactivity = column[Int] ("max_inactivity")
  def latestActivity = column[java.sql.Timestamp] ("latest_activity")
  def ipAddress = column[String] ("ip_address")
  def userAgent = column[String] ("user_agent")

  def userFk =  foreignKey("fk_sessions_users", userId,  TableQuery[UserTableDef] ) (_.id, ForeignKeyAction.Restrict,ForeignKeyAction.Cascade)


  override def * =
    (id , userId, starts, maxInactivity , latestActivity , ipAddress , userAgent ) <>(Session.tupled, Session.unapply)


}
