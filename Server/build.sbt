name := """Budgetr"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

includeFilter in (Assets, LessKeys.less) := "*.less"

excludeFilter in (Assets, LessKeys.less) := "_*.less"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "mysql" % "mysql-connector-java" % "5.1.34",
  "jp.t2v" %% "play2-auth" % "0.13.0",
  "jp.t2v" %% "play2-auth-test" % "0.13.0" % "test",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "com.github.tototoshi" %% "scala-csv" %"1.2.0"
)