name := """shrty"""

version := "0.1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  evolutions,
  "commons-io" % "commons-io" % "2.4",
  "commons-lang" % "commons-lang" % "2.6",
  "commons-validator" % "commons-validator" % "1.3.1",
  "org.postgresql" % "postgresql" % "9.4-1202-jdbc42",
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars.bower" % "jquery" % "2.1.3",
  "org.webjars.bower" % "bootstrap" % "3.3.4",
  "org.webjars.bower" % "highlightjs" % "8.5.0",
  "com.github.rjeschke" % "txtmark" % "0.13",
  "be.objectify" %% "deadbolt-java" % "2.4.1",
  "org.hashids" % "hashids" % "1.0.1",
  "redis.clients" % "jedis" % "2.7.2"
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

// set the debug level for the Ebean infrastructure
playEbeanDebugLevel := 6

// We need a different configuration file for testing
javaOptions in Test += "-Dconfig.resource=application_test.conf"