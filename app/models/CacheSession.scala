package models

import java.sql.Timestamp

import models.SessionType.SessionToken
import models.enums.UserRole

case class CacheSession( id : SessionToken, starts : Timestamp , latestActivity : Timestamp, ipAddress: String , userAgent : String, user : User , roles : Set[UserRole] , maxInactivity : Int)
