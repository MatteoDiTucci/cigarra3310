name := """cigarra3310"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.4"

crossScalaVersions := Seq("2.11.12", "2.12.4")

libraryDependencies ++= Seq(guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  jdbc,
  "org.playframework.anorm" %% "anorm" % "2.6.0",
  "mysql" % "mysql-connector-java" % "8.0.11",
  "org.mockito" % "mockito-core" % "2.16.0" % Test)
