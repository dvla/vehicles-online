// Do not set this to Debug level as it will fail the Travis build when open sourcing due to it being too verbose
logLevel := Level.Info

// Our plugin resolvers
resolvers += "Nexus snapshots" at "http://rep002-01.skyscape.preview-dvla.co.uk:8081/nexus/content/repositories/snapshots"

resolvers += "Nexus releases" at "http://rep002-01.skyscape.preview-dvla.co.uk:8081/nexus/content/repositories/releases"

resolvers += Resolver.url("GitHub repository", url("http://shaggyyeti.github.io/releases"))(Resolver.ivyStylePatterns)

// The Typesafe repository
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"

resolvers += "Maven 2" at "http://repo2.maven.org/maven2"

addSbtPlugin("dvla" % "build-details-generator" % "1.3.2-SNAPSHOT")

addSbtPlugin("dvla" % "microservices-sandbox" % "1.3.9-SNAPSHOT")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.9")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.5")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

addSbtPlugin("default" % "sbt-sass" % "0.1.9")

addSbtPlugin("de.johoop" % "jacoco4sbt" % "2.1.5")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.4.0")

resolvers += "Nexus Repository" at "http://rep002-01.skyscape.preview-dvla.co.uk:8081/nexus/content/repositories/thirdparty/"

resolvers += "Templemore Repository" at "http://templemore.co.uk/repo/"

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

// Plugin for publishing scoverage results to coveralls
addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "0.98.2")

addSbtPlugin("com.sksamuel.scoverage" %% "sbt-coveralls" % "0.0.5")

addSbtPlugin("io.gatling" % "gatling-sbt" % "2.1.0")

