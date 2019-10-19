package models

import java.sql.Timestamp
import java.text.SimpleDateFormat

import models.UsersId.UserId
import play.api.libs.json.{Format, JsString, JsSuccess, JsValue, Json}


case class AffectedUserRole(id : Long  , userId : UserId , roleId : Long  , affectionDate : Timestamp) extends BaseEntity[Long]


object AffectedUserRole {




  implicit object timestampFormat extends Format[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
    def reads(json: JsValue) = {
      val str = json.as[String]
      JsSuccess(new Timestamp(format.parse(str).getTime))
    }
    def writes(ts: Timestamp) = JsString(format.format(ts))
  }

  implicit val affectedRoleFormat = Json.format[AffectedUserRole]
}