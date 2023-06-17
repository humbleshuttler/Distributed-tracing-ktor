package org.humbleshuttler

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.humbleshuttler.plugins.configureMonitoring
import org.humbleshuttler.plugins.configureRouting
import org.humbleshuttler.plugins.configureSerialization
import java.util.logging.LogManager

fun main() {
  val classLoader = Thread.currentThread().contextClassLoader
  val logManager = LogManager.getLogManager()
  logManager.readConfiguration(classLoader.getResourceAsStream("logging.properties"))
  embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
      .start(wait = true)

}

fun Application.module() {
  configureMonitoring()
  configureSerialization()
  configureRouting()
}
