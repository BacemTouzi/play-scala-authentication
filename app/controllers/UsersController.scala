package controllers


import actions.SecuredAction
import com.github.t3hnar.bcrypt._
import javax.inject.{Inject, Singleton}
import models.User
import models.UsersId.UserId
import models.enums.Permission
import play.api.cache.SyncCacheApi
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, _}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.{Configuration, mvc}
import services.{RoleService, SessionService, UserService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class UsersController @Inject()(cc: ControllerComponents , userService: UserService, roleService : RoleService , sessionService: SessionService , cache : SyncCacheApi)(implicit val conf:Configuration = Configuration.reference) extends AbstractController(cc) {





  private def updateForm: Form[User] = Form {
    mapping(
      "id" -> longNumber,
      "username" -> nonEmptyText,
      "firstname" -> nonEmptyText,
      "lastname" -> nonEmptyText,
      "mobile"   -> longNumber,
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    )(User.apply)(User.unapply)
  }





 case class ConstrainedUser(userName: String, firstName: String, lastName: String, mobile: Long, email: String , password: String)


  private def userForm: Form[ConstrainedUser] = Form {
    mapping(
       "username" -> nonEmptyText,
      "firstname" -> nonEmptyText,
      "lastname" -> nonEmptyText,
      "mobile"   -> longNumber,
      "email" -> nonEmptyText,
      "password" -> nonEmptyText
    )(ConstrainedUser.apply)(ConstrainedUser.unapply)
  }


  /********************************************/

  def createUSer()   = SecuredAction (new mvc.BodyParsers.Default(cc.parsers) ,
    Set() , roleService ,sessionService , userService,cache).async{ implicit  request =>

   userForm.bindFromRequest.fold(
      ex => {
        Future(BadRequest(ex.errors.toString))
      },
      valid =>  {
        val user =   User(10 , valid.userName, valid.firstName, valid.lastName, valid.mobile, valid.email, valid.password.bcrypt)

        userService.createUser(user).map{

        case Some(user) => Created(Json.toJson(user))

        case None => BadRequest("cannot create user ! ")

  }})
    }

/*********************************/

  def getUserDetails (id : UserId) = SecuredAction (new mvc.BodyParsers.Default(cc.parsers) ,
    Set() , roleService ,sessionService , userService , cache ).async{

    userService.getUserById(id).map {

      case Some(User(id, userName, firstName, lastName, mobile, email, password)) => Ok(Json.toJson(User(id, userName, firstName, lastName, mobile, email, password)))

      case None => NotFound("No user with such id was found")

    }
  }
/************************************/

    def getUsersByRole (role : Long) =  SecuredAction (new mvc.BodyParsers.Default(cc.parsers) ,
      Set( Permission.ReadUser , Permission.ReadRoles ) , roleService ,sessionService , userService,cache).async{
      userService.getUsersByRoleId(role).map{
        users => Ok(Json.toJson(users))
      }

    }

/*****************************/



  def getAllUsers =  SecuredAction (new mvc.BodyParsers.Default(cc.parsers) ,
    Set( Permission.values() : _* ) , roleService ,sessionService , userService,cache).async {
    implicit request => val currentUser = request.attrs.get(tools.Attrs.PassedUser)
         userService.getAllUsers.map {
        users => Ok(Json.toJson(users))
      }
  }


/************************************************/

  def getRolesByUserId(id : UserId) = SecuredAction (new mvc.BodyParsers.Default(cc.parsers) ,
    Set( Permission.values() : _* ) , roleService ,sessionService , userService,cache).async {

    roleService.getRolesByUserId(id).map{
      roles => Ok(Json.toJson(roles))
    }

  }

/********************************************************/

  def updateUser() =  SecuredAction (new mvc.BodyParsers.Default(cc.parsers) ,
    Set(Permission.UpdateUser , Permission.ReadUser ) , roleService ,sessionService , userService,cache).async{
    implicit  request =>

    updateForm.bindFromRequest.fold(
      ex => {
        Future(BadRequest("invalid input"))
      },
      valid => {
        valid.password = valid.password.bcrypt

        import models.User.userFormat

        userService.getUserById(valid.id).flatMap(req => userService.updateUser(valid) )
          .map{
            case  u :   Some[User]=>  {
              sessionService.getSessionIdFromUserId(valid.id).map{
                case Some(value) => {cache.remove(value.toString)
                 }
              }
              Ok(Json.toJson(u))}
            case _ => BadRequest("cannot update object")
        }
        }
    )}

  /********************************************************/


  def deleteUser(id : UserId)  = SecuredAction (new mvc.BodyParsers.Default(cc.parsers) ,
    Set(Permission.DeleteUser , Permission.AddUser ) , roleService ,sessionService , userService,cache).async {

    userService.getUserById(id).flatMap{
      case None => Future.successful(NotFound("No user found"))
      case _ => userService.delete(id).map{
            case 0 => BadRequest("Cannot delete user")
            case _ => {
              sessionService.getSessionIdFromUserId(id).map{
                case Some(value) => {cache.remove(value.toString)
                }
              }
              Ok("user deleted ")}
          }
    }

  }



}
