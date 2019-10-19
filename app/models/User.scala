package models

 import play.api.libs.json._

case class User (id:UsersId.UserId ,  userName:String,  firstName : String ,   lastName : String,
                 var  mobile : Long, var  email : String , var password: String ) extends  BaseEntity[UsersId.UserId]


object User {
  implicit val userFormat = Json.format[User]
}

object  UsersId {
  type UserId = Long
}