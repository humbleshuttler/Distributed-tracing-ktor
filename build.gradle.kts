val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
  kotlin("jvm") version "1.8.22"
  id("io.ktor.plugin") version "2.3.1"
}

group = "org.humbleshuttler"

version = "0.0.1"

application {
  mainClass.set("org.humbleshuttler.ApplicationKt")

  val isDevelopment: Boolean = project.ext.has("development")
    val homeDir: String = System.getenv("HOME")

  applicationDefaultJvmArgs =
      listOf(
          "-Dio.ktor.development=$isDevelopment",
          "-javaagent:$homeDir/Downloads/opentelemetry-javaagent.jar",
          "-Dotel.exporter.otlp.endpoint=http://172.19.0.1:4317",
          "-Dotel.resource.attributes=service.name=Distributed-tracing-sample")
}

repositories { mavenCentral() }

dependencies {
  implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
  implementation("io.ktor:ktor-serialization-gson-jvm:$ktor_version")
  implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
  implementation("org.slf4j:slf4j-jdk14:2.0.5")
  implementation("io.opentelemetry:opentelemetry-api:1.27.0")
  implementation("io.opentelemetry:opentelemetry-sdk:1.27.0")
  implementation("io.opentelemetry:opentelemetry-exporter-otlp:1.27.0")
  implementation("io.opentelemetry:opentelemetry-semconv:1.27.0-alpha")
  implementation("ch.qos.logback:logback-classic:$logback_version")
  testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
