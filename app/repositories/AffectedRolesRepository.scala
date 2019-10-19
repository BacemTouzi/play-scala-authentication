package repositories

import javax.inject.Inject
import models.AffectedUserRole
import models.tableDefinitions.AffectedUserRoleTableDef
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext

class AffectedRolesRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends BaseRepository[AffectedUserRole, AffectedUserRoleTableDef,Long](dbConfigProvider, executionContext, TableQuery[AffectedUserRoleTableDef]) {



}
