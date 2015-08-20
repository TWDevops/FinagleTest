name := "finagletest"

version := "1.0"

fork in run := true

resolvers += "twitter" at "http://maven.twttr.com"

libraryDependencies += "com.twitter"  %% "finagle-http" % "6.22.0"

libraryDependencies += "com.twitter" %% "finagle-zipkin" % "6.22.0"

libraryDependencies += "com.twitter" %% "finagle-ostrich4" % "6.22.0"

libraryDependencies += "com.twitter" %% "finagle-serversets" % "6.22.0"

libraryDependencies += "com.twitter" %% "finagle-thriftmux" % "6.22.0"

libraryDependencies += "com.twitter" %% "finagle-memcached" % "6.22.0"

//libraryDependencies += "com.twitter.common.zookeeper" % "server-set" % "1.0.9"

com.twitter.scrooge.ScroogeSBT.newSettings

libraryDependencies += "com.twitter" %% "scrooge-core" % "3.16.1"

//assemblyMergeStrategy in assembly := {
 // case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
 // case _         => MergeStrategy.first
//}

javaOptions in run += "-Dcom.twitter.finagle.zipkin.host=172.17.0.7:9410"

javaOptions in run += "-Dcom.twitter.finagle.zipkin.initialSampleRate=1.0"
