package services

import com.google.inject.Inject
import models.{AffectedUserRole, Role}
import models.UsersId.UserId
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import repositories.{AffectedRolesRepository, RoleRepository}
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class RoleService @Inject() (val dbConfigProvider: DatabaseConfigProvider,  executionContext: ExecutionContext , roles : RoleRepository , affectedRoles : AffectedRolesRepository  )
                             extends HasDatabaseConfigProvider[JdbcProfile]  {


  def getAllRoles() = db.run(roles.all())

  def getRolesByUserId ( id : UserId) : Future[Seq[Role]] = {
        db.run(roles.getRolesByUserId(id))
  }


  def getRoleById(role: Long) = db.run(roles.find(role))


  def affect (roleAffection : AffectedUserRole) = {
    db.run(affectedRoles.save(roleAffection : AffectedUserRole)(executionContext))
  }

  def deleteRoleFromUser(roleId : Long , userId: UserId) = {
    db.run(roles.deleteRoleFromUser(roleId,userId))
  }

  def getRoleAffectionByUserIdAndRoleId(roleId : Long , userId: UserId) : Future[Option[AffectedUserRole]] = {
    db.run(roles.getAffectionByRoleIdAndUserId(roleId,userId))
  }


}
