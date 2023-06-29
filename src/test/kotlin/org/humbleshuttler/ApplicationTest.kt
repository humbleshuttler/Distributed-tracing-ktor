package org.humbleshuttler

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.testing.*
import kotlin.test.*
import io.ktor.http.*
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.metrics.LongCounter
import kotlinx.coroutines.*
import org.humbleshuttler.plugins.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.system.measureTimeMillis

class ApplicationTest {

    val logger: Logger = Logger.getLogger("ApplicationTest")

    val commonClientAttributes: Attributes = Attributes.of(
        AttributeKey.stringKey("Method"), "Get",
        AttributeKey.stringKey("Type"), "Client",
    )
    val responseCounter: LongCounter =
        OpenTelemetry.getMeter().counterBuilder("responses").setDescription("Response count").setUnit("1").build()
    val responseTimeCollector = mutableListOf<Long>()

    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }

    @Test
    fun testThunderingHerd() = runBlocking<Unit> {
        val totalClients = 100
        val defers = List(totalClients) {
            val httpClient = HttpClient.newHttpClient()
            val httpRequest = HttpRequest.newBuilder(URI.create("http://127.0.0.1:8080/thundering_herd")).GET().build()
            async {
                makerequest(httpClient, httpRequest, 10)
            }
        }
        defers.awaitAll()
        logger.log(Level.INFO, "all request completed")
        responseTimeCollector.sort()
        logger.log(Level.INFO, "top 10 slowest response time: ${responseTimeCollector.takeLast(10)}")
    }

    private suspend fun makerequest(client: HttpClient, request: HttpRequest, numberOfRequests: Int) {
        for (i in 1..numberOfRequests) {

            val attribute =
                commonClientAttributes.toBuilder().put(AttributeKey.stringKey("Endpoint"), "/thundering_herd").build()
            requestCounter.add(
                1,
                attribute
            )

            var response: HttpResponse<String>
            logger.log(Level.INFO, "Request: $request from ${Thread.currentThread()}")
            val responseTime = measureTimeMillis {
                response = client.send(request, BodyHandlers.ofString())
            }
            logger.log(Level.INFO, "Response received: $response at ${Thread.currentThread()}")
            responseTimeCollector.add(responseTime)
            responseCounter.add(
                1,
                attribute.toBuilder().put(AttributeKey.longKey("StatusCode"), response.statusCode().toLong()).build()
            )
            delay(Duration.ofSeconds(0.1.toLong()).toMillis())
        }
    }
}
