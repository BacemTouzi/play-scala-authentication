package services

import com.google.inject.Inject
import models.SessionType.SessionToken
import models.User
import models.UsersId.UserId
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repositories.UserRepository
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
class UserService @Inject() (val dbConfigProvider: DatabaseConfigProvider, executionContext: ExecutionContext , users : UserRepository) extends HasDatabaseConfigProvider[JdbcProfile]  {

  def delete(id: UserId)  = db.run(users.deleteById(id))

  def createUser(user : User) = db.run(users.save(user)(executionContext))

  def getAllUsers = db.run(users.all())

  def getUserById(id: UserId) = db.run(users.find(id))

  def authenticateUser (username : String   ) =  db.run(users.searchByUsername(username ))

  def getUserIdByToken(token : SessionToken) : Future[Option[UserId]] = db.run(users.findUserIdByToken(token))

  def getUsersByRoleId (id : Long) = db.run(users.getByRoleId(id))

  def updateUser(user:User) = db.run(users.updateUser(user))

}
