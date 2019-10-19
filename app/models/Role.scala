package models

 import models.enums.UserRole

import play.api.libs.json._


case class Role( id : Long ,  name: UserRole) extends  BaseEntity[Long]

object Role {

 implicit def myEnumFormat = new Format[UserRole] {
  def reads(json: JsValue) = JsSuccess(UserRole.valueOf(json.as[String]))
  def writes(myEnum: UserRole) = JsString(myEnum.toString)
 }
     //implicit val userRoleFormat = Json.format[UserRole]

  implicit val roleFormat = Json.format[Role]

 }