package tools

import play.api.libs.typedmap.TypedKey


object Attrs {

  val PassedUser: TypedKey[java.io.Serializable] = TypedKey("AuthenticatedAs") //  authenticated user object  passed to controller

  //val PassedUserId: TypedKey[UserId] = TypedKey("AuthenticatedAs") // authenticated user's id gets added to request attributes , so it can be used in controller

}

object SessionProperties {

  val MAX_SERVER_SIDE_SESSION_INACTIVITY = 60*60*24*14 // in seconds , should be bigger than cookie's max age.

}