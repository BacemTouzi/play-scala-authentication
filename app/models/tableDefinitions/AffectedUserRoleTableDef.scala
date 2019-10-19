package models.tableDefinitions

import java.sql.Timestamp

import models.AffectedUserRole
import slick.jdbc.MySQLProfile.api._
import slick.lifted.Tag
import slick.model.ForeignKeyAction



class AffectedUserRoleTableDef (tag : Tag) extends BaseTable[AffectedUserRole](tag, "affected_roles") with TableId[Long] {

    override def id =   column[Long]("id", O.PrimaryKey, O.Unique, O.AutoInc)

    def userId  = column[Long]("user_id")

    def roleId = column[Long]("role_id")

    def affectionDate = column[Timestamp]("affection_date")

    def roleFk =  foreignKey("fk_affected_roles_role", roleId,  TableQuery[RoleTableDef] ) (_.id, ForeignKeyAction.NoAction,ForeignKeyAction.Cascade)

    def userFk =  foreignKey("fk_affected_roles_user", userId,  TableQuery[UserTableDef] ) (_.id, ForeignKeyAction.NoAction,ForeignKeyAction.Cascade)


    override def * =
    (id,userId,roleId,affectionDate) <>((AffectedUserRole.apply _).tupled, AffectedUserRole.unapply)









}

