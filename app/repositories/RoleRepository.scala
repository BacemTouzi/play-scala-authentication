package repositories

import javax.inject.Inject
import models.{AffectedUserRole, Role}
import models.UsersId.UserId
import models.tableDefinitions.{AffectedUserRoleTableDef, RoleTableDef}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext

class RoleRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends BaseRepository[Role, RoleTableDef,Long](dbConfigProvider, executionContext, TableQuery[RoleTableDef]) {


  def getRolesByUserId(id: UserId): DBIOAction[Seq[Role], slick.dbio.NoStream, Effect.Read] = {
    val roles = TableQuery[RoleTableDef]
    TableQuery[AffectedUserRoleTableDef].filter(_.userId === id)
      .join(roles).on(_.roleId === _.id).map(_._2)
      .result
  }

  def getAffectionByRoleIdAndUserId(roleId: Long, userId: UserId): DBIO[Option[AffectedUserRole]] = {
    TableQuery[AffectedUserRoleTableDef].filter(_.userId === userId).filter(_.roleId === roleId).result.headOption
  }



  def deleteRoleFromUser(role: Long, user: UserId) =     TableQuery[AffectedUserRoleTableDef].filter(_.userId === user).filter(_.roleId === role).delete



  }


