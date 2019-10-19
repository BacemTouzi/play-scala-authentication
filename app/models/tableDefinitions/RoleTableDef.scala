package models.tableDefinitions

import models.Role
import models.enums.UserRole
import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag



class RoleTableDef(tag: Tag) extends BaseTable[Role](tag, "roles") with TableId[Long] {

  override def id : Rep[Long] =   column[Long]("id", O.PrimaryKey, O.Unique, O.AutoInc)
  implicit  val   columnType : BaseColumnType[UserRole] = MappedColumnType.base[UserRole , String]({r => r.toString},{str => UserRole.valueOf(str)})
  def name  =  column[UserRole]("name")

   override def * =
    (id,name) <>((Role.apply _).tupled, Role.unapply )

}