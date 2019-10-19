package  actions

import java.sql.Timestamp
import java.time.Instant

import javax.inject.Inject
import models.SessionType.SessionToken
import models.enums.{Permission, UserRole}
import models.{CacheSession, User}
import play.api.cache.SyncCacheApi
import play.api.mvc._
import security.BearerTokenGenerator
import services.{RoleService, SessionService, UserService}

import scala.collection.JavaConverters._
import scala.concurrent.{Await, ExecutionContext, Future};


class SecuredAction @Inject()(parser: BodyParsers.Default, neededPermissions : Set[Permission] , roleService: RoleService, sessionService: SessionService, userService: UserService, cache : SyncCacheApi )(implicit val ec: ExecutionContext  ) extends ActionBuilderImpl(parser) {

//todo use asyncCacheApi

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {

    extractToken(request).flatMap(cache.get[CacheSession]) match {

      case None =>{
        println("*********** Not in cache")

        extractToken(request).flatMap(findUser).fold(
          //no session in db
          if (request.path.contains("login")) block(request)
          else
            Future.successful(Results.Unauthorized("you're unauthorized to perform this act , you must login first  !! ")) // token was invalid - return 401


        ) { case (session, user, roles) => //session found in db
          if (request.path.contains("login") && !request.session.isEmpty) Future.successful(Results.BadRequest("You're already logged in "))
          else {
            val currentPermissions = userPermissions(roles)
            if (currentPermissions.intersect(neededPermissions) != neededPermissions) {
              Future.successful(Results.Forbidden("you're not allowed to perform this act")) // return 403
            } else {
              import tools.Attrs
              val newReq = request.addAttr(Attrs.PassedUser, user) //add user to request attributes
              if (request.path.contains("/logout")) //do not extend session if request comes from the logout method
                block(newReq)
              else {
                // add session to cache
                val cachedSession = CacheSession(session.id, // BearerTokenGenerator.md5(extractToken(request).get.toString)
                  session.starts, session.latestActivity, session.ipAddress, session.userAgent, user,roles , session.maxInactivity)

               // cache.remove(cachedSession.id)
                cache.set(cachedSession.id, cachedSession) // todo set key to  expire after x time ( x < maxInactivity)
                println("last Added to cache ****** : " + cache.get(cachedSession.id))
                block(newReq).map { // allow the request ,create new cookie with a new timeout ( equal to maxAge in application.conf  )
                  result => result.withSession("tokenId" -> request.session.data("tokenId"))
                }
              }
            }
          }


        }


      }

      case Some(cacheSession) => { // if session already in cache
        if (request.path.contains("login") && !request.session.isEmpty) Future.successful(Results.BadRequest("You're already logged in ")) // if session exists in cache and the calling method is login prevent the user from loging in twice
        else {         println("*********** Found in  cache")
          //check permissions from roles already stored in cache
          //any role change ( retrieve role , or add role to the current user ( in rolesController ) )  will result in deleting this cache , so in the next request he will hit the database again to get the updated roles
          val currentPermissions = userPermissions(cacheSession.roles)
          if (currentPermissions.intersect(neededPermissions) != neededPermissions) {
            Future.successful(Results.Forbidden("you're not allowed to perform this act")) // return 403
          }
          else {
          import tools.Attrs
          val nreq = request.addAttr(Attrs.PassedUser, cacheSession.user) // add the current user to the request attributes to be used later in controller
            val updatedCacheSession = CacheSession(cacheSession.id,cacheSession.starts, Timestamp.from(Instant.now),cacheSession.ipAddress,cacheSession.userAgent,cacheSession.user,cacheSession.roles , cacheSession.maxInactivity  )
            // update  cache
            cache.remove(cacheSession.id) //
            cache.set(cacheSession.id, updatedCacheSession)
            //update in database

            sessionService.updateSession(models.Session(updatedCacheSession.id ,updatedCacheSession.user.id , updatedCacheSession.starts,updatedCacheSession.maxInactivity , updatedCacheSession.latestActivity,updatedCacheSession.ipAddress,updatedCacheSession.userAgent))

            println("******** last Activity from cache : " + cache.get(updatedCacheSession.id).get.asInstanceOf[CacheSession].latestActivity.toString)

            block(nreq).map {
            result => result.withSession("tokenId" -> request.session.data("tokenId")) // new cookie to extend session timeout
          }
        }

        }
      }

    }

  }




  private def extractToken[A](request: Request[A]): Option[SessionToken] = {
   request.session.get("tokenId") match {
     case None => None
     case Some(value) => Some(BearerTokenGenerator.md5(value))
   }
  }


  private def userPermissions(userRoles: Set[UserRole]): Set[Permission] = {
    userRoles.flatMap(_.permissions.asScala)
  }


  private def getUserWithHisRoles(token: SessionToken): Option[(models.Session , User, Set[UserRole])] = {

    var sessionVar : models.Session = null

    val f = sessionService.getSession(token)
      .flatMap {
      case None => Future.successful(None)
      case Some(session) => {

        for {

          user <-
            userService.getUserById(session.userId)

          update <- {
            val newSession = models.Session(token, session.userId, session.starts,
              session.maxInactivity, Timestamp.from(Instant.now), session.ipAddress, session.userAgent)
            sessionVar = newSession
            sessionService.updateSession(newSession)
          }

          rls <- roleService.getRolesByUserId(session.userId).map {
            roles => {
              val y = for (role <- roles) yield role.name
              val set = y.toSet
              Some( sessionVar , user.get, set )
            }

          }

        } yield rls

      }

    }



    import scala.concurrent.duration._
    Await.result(f, 8.seconds) //todo !! should be non blocking ?? WS ?

  }



  private def findUser(token: SessionToken): Option[(models.Session , User, Set[UserRole])] = {

    getUserWithHisRoles(token)  match {

      case None => None

      case t =>  {
        Some(t.get)
      }
    }

  }





}


import scala.concurrent.ExecutionContext.Implicits.global

object SecuredAction {

  def apply(parsers : BodyParsers.Default , permissions: Set[Permission] ,    roleService: RoleService , sessionService: SessionService, userService: UserService , cacheApi: SyncCacheApi)   = {

    new SecuredAction(parsers , permissions , roleService , sessionService , userService , cacheApi )

  }





}


