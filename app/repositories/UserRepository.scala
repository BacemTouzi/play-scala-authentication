package repositories

import javax.inject.Inject
import models.SessionType.SessionToken
import models.User
import models.UsersId.UserId
import models.tableDefinitions.{AffectedUserRoleTableDef, SessionTableDef, UserTableDef}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext


class UserRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends BaseRepository[User, UserTableDef,UserId](dbConfigProvider, executionContext, TableQuery[UserTableDef]) {



 def getByRoleId(id: Long) = {
  TableQuery[AffectedUserRoleTableDef].filter(_.roleId === id ).join(TableQuery[UserTableDef]).on(_.userId === _.id).map(_._2).result
 }

 def searchByUsername  (name : String   ) : DBIO[Option[User]] = TableQuery[UserTableDef].filter(e=> e.userName === name).result.headOption


 def findUserIdByToken (token : SessionToken)   = {

  val sessions = TableQuery[SessionTableDef]

  TableQuery[UserTableDef]
     .join(sessions).on(_.id === _.userId)
    .filter( _._2.id === token)
    .map(_._1.id ).result.headOption
 }


 def updateUser(user : User) = update(user.id,user)

 }
















/*
def update(id: Long, c: Project): Future[Option[Project]] = {
 val q = filterByIdQuery(id).map(_.writableFields)
 .update(Projects.mapFormToTable(c))
 (db run q).flatMap(
 affected =>
 if (affected > 0) {
 findOneById(id)
} else {
 Future(None)
}
 )
}
}*/