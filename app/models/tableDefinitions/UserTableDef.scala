package models.tableDefinitions

import models.User
import models.UsersId.UserId
import slick.lifted.Tag
import slick.jdbc.MySQLProfile.api._


class UserTableDef(tag: Tag) extends BaseTable[User](tag, "users")  with TableId[UserId] {


  override  def id = column[Long]("id", O.PrimaryKey, O.Unique, O.AutoInc)

  def userName = column[String]("username",O.Unique)
  def firstName = column[String]("first_name")
  def lastName = column[String]("last_name")
  def mobile = column[Long]("mobile")
  def email = column[String]("email")
  def password = column[String]("password")


  override def * =
    (id,userName, firstName, lastName, mobile, email, password) <> ((User.apply _).tupled, User.unapply)

}