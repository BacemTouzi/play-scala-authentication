package  actions

import java.sql.Timestamp
import java.time.Instant

import javax.inject.Inject
import models.SessionType.SessionToken
import models.User
import models.enums.{Permission, UserRole}
import play.api.mvc._
import security.BearerTokenGenerator
import services.{RoleService, SessionService, UserService}

import scala.collection.JavaConverters._
import scala.concurrent.{Await, ExecutionContext, Future};

   class AuthenticatedRequest[A](user: User , request: Request[A]) extends WrappedRequest(request)

    class AuthAction @Inject() (parser: BodyParsers.Default , neededPermissions : Set[Permission] ,    roleService: RoleService ,    sessionService: SessionService , userService: UserService ) (implicit val ec: ExecutionContext  ) extends ActionBuilderImpl(parser) {

//database only

      override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {

        extractToken(request).flatMap(findUser).fold(
          if (request.path.contains("login"))  block(request)

          else

            Future.successful(Results.Unauthorized("you're unauthorized to perform this act , you must login first  !! ")) // token was invalid - return 401
        ) { case (user,roles) =>
          if (request.path.contains("login") && !request.session.isEmpty) Future.successful(Results.BadRequest("You're already logged in "))
          else {
          val currentPermissions = userPermissions(roles)
          if (currentPermissions.intersect(neededPermissions) != neededPermissions) {
            Future.successful(Results.Forbidden("you're not allowed to perform this act")) // return 403
          } else {
            import tools.Attrs
            val newReq = request.addAttr(Attrs.PassedUser, user)
            if (request.path.contains("/logout")) //do not extend session if request comes from the logout method
              block(newReq)
              else
            block(newReq).map {
              result => result.withSession("tokenId" -> request.session.data("tokenId")) //extend session timeout
            }

          }
        }

        }

      }


      private def extractToken[A](request: Request[A]): Option[SessionToken] = {
        request.session.get("tokenId")
      }


      private def userPermissions(userRoles: Set[UserRole]): Set[Permission] = {
        userRoles.flatMap(_.permissions.asScala)
      }


      private def getUserWithHisRoles(token: SessionToken): Option[(User, Set[UserRole])] = {

        val f = sessionService.getSession(BearerTokenGenerator.md5(token)).flatMap {
          case None => Future.successful(None)
          case Some(session) => {

            for {
              user <-
                userService.getUserById(session.userId)

              update <- {
                val newSession = models.Session(BearerTokenGenerator.md5(token), session.userId, session.starts,
                  session.maxInactivity, Timestamp.from(Instant.now), session.ipAddress, session.userAgent)
                sessionService.updateSession(newSession)
              }

              rls <- roleService.getRolesByUserId(session.userId).map {
                roles => {
                  val y = for (role <- roles) yield role.name
                  val set = y.toSet
                  Some(user.get, set)
                }

              }

            } yield rls

          }

        }



        import scala.concurrent.duration._
        Await.result(f, 8.seconds) // !! should be non blocking ??

      }



      private def findUser(token: SessionToken): Option[(User, Set[UserRole])] = {

       getUserWithHisRoles(token)  match {

         case None => None

          case t =>  {
              Some(t.get)
          }
        }

      }
    }


import scala.concurrent.ExecutionContext.Implicits.global

object AuthAction {

  def apply(parsers : BodyParsers.Default , permissions: Set[Permission] ,     roleService: RoleService , sessionService: SessionService, userService: UserService)   = {

     new AuthAction(parsers , permissions , roleService , sessionService , userService)

  }

}


