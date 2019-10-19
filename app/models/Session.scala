package models

import java.sql.Timestamp

import models.SessionType.SessionToken
import models.UsersId.UserId
import play.api.libs.json._
import java.sql.Timestamp
import java.sql.Timestamp
import java.text.SimpleDateFormat

import models.enums.UserRole
import play.api.Play.current
import play.api.libs.json._




case class Session(id: SessionToken , userId : UserId, starts : Timestamp, maxInactivity : Int , latestActivity : Timestamp ,
                   ipAddress: String , userAgent : String) extends  BaseEntity[SessionToken] {
}

object SessionType {
  type SessionToken = String
}

object Session {


  implicit object timestampFormat extends Format[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
    def reads(json: JsValue) = {
      val str = json.as[String]
      JsSuccess(new Timestamp(format.parse(str).getTime))
    }
    def writes(ts: Timestamp) = JsString(format.format(ts))
  }

  implicit val sessionFormat = Json.format[Session]
    val tupled = (this.apply _).tupled
}
