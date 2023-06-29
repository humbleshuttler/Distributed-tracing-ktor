package org.humbleshuttler.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.opentelemetry.api.common.AttributeKey.stringKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.metrics.LongCounter
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement
import java.time.Instant
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import org.humbleshuttler.OpenTelemetry

val random = Random(Instant.now().nano)
val logger: Logger = Logger.getLogger("Application.Routing")
val processingTimeMetrics: ObservableDoubleMeasurement =
    OpenTelemetry.getMeter().gaugeBuilder("processing_time").setDescription("Request processing time").setUnit("ms")
        .buildObserver()
val requestCounter: LongCounter =
    OpenTelemetry.getMeter().counterBuilder("requests").setDescription("Request count").setUnit("1").build()
val commonServerAttributes: Attributes = Attributes.of(stringKey("Method"), "Get", stringKey("Type"), "Server")
val mutex = Mutex()

fun Application.configureRouting() {
    routing {
        get("/") {
            requestCounter.add(
                1,
                commonServerAttributes.toBuilder().put(stringKey("Endpoint"), "/").build()
            )
            val delay = random.nextDouble(500.0)
            logger.log(Level.INFO, "Processing time: $delay")
            processingTimeMetrics.record(delay, commonServerAttributes)
            call.respondText("Hello World!")
        }
        get("/random") {
            val delay = random.nextDouble(500.0)
            logger.log(Level.INFO, "Processing time: $delay")
            //          processingTimeMetrics.record(delay, getMethodAttribute)
            delay(delay.toLong())
            call.respondText(delay.toString() + "\n")
        }
        get("/thundering_herd") {
            requestCounter.add(
                1,
                commonServerAttributes.toBuilder().put(stringKey("Endpoint"), "/thundering_herd").build()
            )
            val delay = random.nextDouble(100.0)
            mutex.lock()
            delay(delay.toLong())
            mutex.unlock()
            logger.log(Level.INFO, "Processing time: $delay")
            call.respondText("from the herd")
        }
    }
}
