name := "swaggeriheartradio"
 
version := "1.0" 
      
lazy val `swaggeriheartradio` = (project in file(".")).enablePlugins(PlayScala,SwaggerPlugin)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "https://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( evolutions , cacheApi  , ehcache , ws , specs2 % Test , guice , "com.pauldijou" %% "jwt-core" % "3.1.0" ,   "com.pauldijou" %% "jwt-play" % "3.1.0" ,
  "com.typesafe.play" %% "play-slick" % "4.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "4.0.0",
  "mysql" % "mysql-connector-java" % "8.0.15"


)
libraryDependencies += "org.scala-lang.modules" %% "scala-async" % "0.10.0"
// enable Play cache API (based on your Play version)
libraryDependencies += play.sbt.PlayImport.cacheApi

//unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

resolvers += Resolver.bintrayRepo("iheartradio", "maven")

swaggerDomainNameSpaces := Seq("models")

libraryDependencies += "com.github.t3hnar" %% "scala-bcrypt" % "4.1"


//libraryDependencies += "com.typesafe.play" %% "play-test" % "2.7.0"
