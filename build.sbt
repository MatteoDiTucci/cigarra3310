name := """play-scala-starter-example"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.sonatypeRepo("snapshots")

scalaVersion := "2.12.4"

crossScalaVersions := Seq("2.11.12", "2.12.4")

libraryDependencies ++= Seq(guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test,
  jdbc,
  "org.playframework.anorm" %% "anorm" % "2.6.0",
  "org.xerial" % "sqlite-jdbc" % "3.8.6",
  "org.mockito" % "mockito-core" % "2.16.0" % Test)
