name := "data-source-parameters"

version := "0.1"

scalaVersion := "2.13.6"

val AnormVersion = "2.6.10"
val CirceVersion = "0.13.0"

libraryDependencies ++= Seq(
  "org.postgresql"           % "postgresql"           % "42.2.18",
  "org.playframework.anorm" %% "anorm"                % AnormVersion,
  "com.zaxxer"               % "HikariCP"             % "3.4.5",
  "org.flywaydb"             % "flyway-core"          % "7.5.2",
  "ch.qos.logback"           % "logback-classic"      % "1.2.3",
  "io.circe"                %% "circe-core"           % CirceVersion,
  "io.circe"                %% "circe-generic"        % CirceVersion,
  "io.circe"                %% "circe-generic-extras" % CirceVersion,
  "io.circe"                %% "circe-jawn"           % CirceVersion,
  "io.circe"                %% "circe-parser"         % CirceVersion,
  "org.testcontainers"       % "postgresql"           % "1.15.1" % Test,
  "org.scalatest"           %% "scalatest"            % "3.2.5"  % Test,
  "org.scalamock"           %% "scalamock"            % "4.4.0"  % Test
)
