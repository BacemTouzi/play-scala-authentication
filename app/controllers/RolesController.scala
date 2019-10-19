package controllers

 import java.sql.Timestamp
 import java.time.Instant

 import actions.SecuredAction
 import javax.inject.{Inject, Singleton}
 import models.AffectedUserRole
 import models.UsersId.UserId
 import models.enums.Permission
 import play.api.cache.SyncCacheApi
 import play.api.libs.json.Json
 import play.api.mvc.{AbstractController, ControllerComponents}
 import play.api.{Configuration, mvc}
 import services.{RoleService, SessionService, UserService}

 import scala.concurrent.ExecutionContext.Implicits.global
 import scala.concurrent.Future

@Singleton
class RolesController @Inject()(cc: ControllerComponents, roleService: RoleService , userService: UserService , sessionService: SessionService  , cache : SyncCacheApi)(implicit val conf:Configuration = Configuration.reference) extends AbstractController(cc) {


def getAllRoles  = SecuredAction (new mvc.BodyParsers.Default(cc.parsers) ,
  Set.empty , roleService ,sessionService , userService , cache).async {
  roleService.getAllRoles().map {
    roles => Ok(Json.toJson(roles))
  }
}


  def getRoleById (id : Long ) = SecuredAction (new mvc.BodyParsers.Default(cc.parsers) ,
    Set( Permission.values() : _* ) , roleService ,sessionService , userService , cache).async
   {
    roleService.getRoleById(id).map{

      case None => NotFound("No role found")
      case Some(role) => Ok(Json.toJson(role))

    }


  }

/*******************************************************************/


  def addRoleToUser(role : Long , user : UserId) = SecuredAction (new mvc.BodyParsers.Default(cc.parsers) ,
    Set( Permission.values() : _* ) , roleService ,sessionService , userService , cache).async { request =>

    def f = for {
      u <- userService.getUserById(user)
      r <- roleService.getRoleById(role)
      ar <- roleService.getRoleAffectionByUserIdAndRoleId(role, user)

    } yield (u, r, ar)

    f.flatMap {
      case (_, None, _) => Future(NotFound("No role found"))
      case (None, _, _) => Future(NotFound("No user found "))
      case (Some(_), Some(_), Some(_)) => Future(BadRequest("This Role is already affected to this user , cannot affect same role twice "))
      case _ => roleService.affect(AffectedUserRole(10, user, role, Timestamp.from(Instant.now))).map {

        case None => BadRequest("cannot affect role to the given user ")

        case Some(affection) => {

          sessionService.getSessionIdFromUserId(user).map{
            case Some(value) => {cache.remove(value.toString)
          println("********** deleted for add :"+ cache.get(value.toString))
            }
          }
           Created(Json.toJson(affection))

        }


      }

    }
  }

 /****************************************************************/

  def deleteRoleFromUser(userId: Long, roleId: Long) = SecuredAction (new mvc.BodyParsers.Default(cc.parsers) ,
    Set( Permission.values() : _* ) , roleService ,sessionService , userService , cache).async{ request =>
    def f = for {
      u <- userService.getUserById(userId)
      r <- roleService.getRoleById(roleId)
      ar <- roleService.getRoleAffectionByUserIdAndRoleId(roleId, userId)

    } yield (u, r,ar)

    f.flatMap{
      case (_, None,_) => Future(NotFound("No role found"))
      case (None, _,_) => Future(NotFound("No user found "))
      case (Some(_), Some(_), None) => Future(BadRequest("This user doesn't have this role"))

        case _ =>  roleService.deleteRoleFromUser(roleId,userId)map{
        case 0 => BadRequest("Cannot delete affected role")
        case _ => {

           sessionService.getSessionIdFromUserId(userId).map{
             case Some(value)=> cache.remove(value)
               println("deleted del " + cache.get(value))
          }
           Ok("role deleted , this user won't hold this role anymore ")
      }

        }


    }

  }


}
