package repositories

import javax.inject.Inject
import models.Session
import models.SessionType.SessionToken
import models.UsersId.UserId
import models.tableDefinitions.SessionTableDef
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext



class SessionRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends BaseRepository[models.Session, SessionTableDef,String](dbConfigProvider, executionContext, TableQuery[SessionTableDef])
{
  def findIdByUserId(id: UserId)  = TableQuery[SessionTableDef].filter(_.userId === id).map(_.id).result.headOption


  def getMySessions(id: UserId , token: SessionToken) : DBIOAction[Seq[Session],slick.dbio.NoStream,Effect.Read]  =  TableQuery[SessionTableDef].filter(_.userId === id ).result

  def getSessionByUserId(id: UserId) : DBIOAction[Seq[Session],slick.dbio.NoStream,Effect.Read]   = TableQuery[SessionTableDef].filter(_.userId === id).result

  def updateSession(session: Session) = update(session.id,session)


}





