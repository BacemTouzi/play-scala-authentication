package controllers

import java.sql.Timestamp
import java.time.{Clock, Instant}

import actions.SecuredAction
import com.github.t3hnar.bcrypt._
import javax.inject._
import models.User
import models.UsersId.UserId
import models.enums.Permission
import play.api.cache.SyncCacheApi
import play.api.libs.json.Json
import play.api.mvc._
import play.api.{Configuration, mvc}
import repositories.SessionRepository
import security.BearerTokenGenerator
import services.{RoleService, SessionService, UserService}
import tools.SessionProperties

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents ,roleService: RoleService, userService: UserService , sessionRepository: SessionRepository , sessionService: SessionService , cache: SyncCacheApi) (implicit  val conf:Configuration = Configuration.reference) extends AbstractController(cc) {


  def login(username : String , password : String) = SecuredAction (new mvc.BodyParsers.Default(cc.parsers) ,
    Set() , roleService ,sessionService , userService,cache).async   {
    request =>
      var idV : UserId = 0

     userService.authenticateUser(username)
      .map{
        case Some(User(id, _, _, _, _, _, password2)) => {
          val k = password.isBcrypted(password2) // todo use safe bcrypted method instead
          idV = id

          k match {

case true => {


  implicit val clock: Clock = Clock.systemUTC
   val token = BearerTokenGenerator.generateMyToken(id.toString)
  val hashedToken = BearerTokenGenerator.md5(token)

  import models.Session

   val session2 =  Session(hashedToken ,idV, Timestamp.from(Instant.now) , SessionProperties.MAX_SERVER_SIDE_SESSION_INACTIVITY, Timestamp.from(Instant.now) ,request.remoteAddress , request.headers.get("user-agent")
     .getOrElse("couldn't track user's browser at the time ") )
             //cache.set(token,id)
        sessionService.createSession(session2)

  Ok(token).withSession("tokenId" -> token)


}

case false => Unauthorized("Invalid username or password")
        }}
        case None => Unauthorized("Invalid username or password  ")

      }

     }





  def logout   = SecuredAction (new mvc.BodyParsers.Default(cc.parsers) ,
    Set() , roleService ,sessionService , userService,cache).async {
    implicit request => val currentUser = request.attrs.get(tools.Attrs.PassedUser)


       val v = BearerTokenGenerator.md5(request.session.data("tokenId"))
        sessionService.delete(v).map{

          case 0 => BadRequest("Cannot delete session")

          case _ => {
            cache.remove(BearerTokenGenerator.md5(request.session.data("tokenId").toString))

            Ok(s"You're logged off , See you later ${currentUser.get.asInstanceOf[User].userName}").withNewSession


    }

        }

    }




  def getSessions(id: Long) =  SecuredAction (new mvc.BodyParsers.Default(cc.parsers) ,
    Set(Permission.values : _* ) , roleService ,sessionService , userService,cache).async {


userService.getUserById(id).flatMap{
  case None => Future.successful(NotFound("can't find user with such id "))

  case _ =>
      sessionService.getSessionsByUserId(id).map {
      sessions =>Ok(Json.toJson(sessions))
    }

  }

  }


  def getMyOtherActiveSessions =  SecuredAction (new mvc.BodyParsers.Default(cc.parsers) ,
    Set.empty , roleService ,sessionService , userService,cache).async { request =>

    sessionService.getMyOtherSessions(request.attrs.get(tools.Attrs.PassedUser).get.asInstanceOf[User].id , request.session.data("tokenId")).map{

      sessions => Ok(Json.toJson(sessions))

    }

  }


//todo change password and invalidate other active  sessions

  }