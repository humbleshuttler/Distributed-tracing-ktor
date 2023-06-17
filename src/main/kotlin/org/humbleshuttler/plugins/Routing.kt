package org.humbleshuttler.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.opentelemetry.api.common.AttributeKey.stringKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement
import java.time.Instant
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.random.Random
import kotlinx.coroutines.delay
import org.humbleshuttler.OpenTelemetry

val random = Random(Instant.now().nano)
val logger: Logger = Logger.getLogger("Application.Routing")
val processingTimeMetrics: ObservableDoubleMeasurement =
    OpenTelemetry.getMeter()
        .gaugeBuilder("processing_time")
        .setDescription("Request processing time")
        .setUnit("ms")
        .buildObserver()
val getMethodAttribute: Attributes = Attributes.of(stringKey("Method"), "Get")

fun Application.configureRouting() {
  routing {
    get("/") {
      val delay = random.nextDouble(500.0)
      logger.log(Level.INFO, "Processing time: $delay")
      processingTimeMetrics.record(delay, getMethodAttribute)
      call.respondText("Hello World!")
    }
    get("/random") {
      val delay = random.nextDouble(500.0)
      logger.log(Level.INFO, "Processing time: $delay")
      //          processingTimeMetrics.record(delay, getMethodAttribute)
      delay(delay.toLong())
      call.respondText(delay.toString() + "\n")
    }
  }
}
