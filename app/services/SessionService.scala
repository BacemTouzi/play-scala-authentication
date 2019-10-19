package  services


import com.google.inject.Inject
import models.Session
import models.SessionType.SessionToken
import models.UsersId.UserId
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repositories.SessionRepository
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class SessionService @Inject() (val dbConfigProvider: DatabaseConfigProvider, executionContext: ExecutionContext , sessions : SessionRepository)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  def getMyOtherSessions(id : UserId , token: SessionToken): Future[Seq[Session]] = db.run(sessions.getMySessions(id,token))

  def getSessionsByUserId(id: UserId) = db.run(sessions.getSessionByUserId(id))

  def getSession( id : SessionToken) : Future[Option[Session]]  =  db.run(sessions.find(id))

  def delete(id: SessionToken)  = db.run(sessions.deleteById(id))

  def createSession(session : Session) = db.run(sessions.create(session)(executionContext))

  def updateSession(session: Session) = db.run(sessions.updateSession(session) )

  def getSessionIdFromUserId(id : UserId) = db.run(sessions.findIdByUserId(id))

}

