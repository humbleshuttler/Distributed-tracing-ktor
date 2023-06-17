package org.humbleshuttler

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.metrics.Meter
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes

private const val SCOPE = "Distributed-tracing-sample"

object OpenTelemetry {

  private var openTelemetry: OpenTelemetry
  init {
    val resource: Resource =
        Resource.getDefault()
            .merge(
                Resource.create(
                    Attributes.of(ResourceAttributes.SERVICE_NAME, SCOPE)))

    val sdkTracerProvider: SdkTracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(
                BatchSpanProcessor.builder(OtlpGrpcSpanExporter.builder().build()).build())
            .setResource(resource)
            .build()

    val sdkMeterProvider: SdkMeterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(
                PeriodicMetricReader.builder(OtlpGrpcMetricExporter.builder().build()).build())
            .setResource(resource)
            .build()

    openTelemetry =
        OpenTelemetrySdk.builder()
            .setTracerProvider(sdkTracerProvider)
            .setMeterProvider(sdkMeterProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build()
  }

  fun getTracer(): Tracer {
    return openTelemetry.getTracer(SCOPE, "1.0.0")
  }

    fun getMeter(): Meter {
        return openTelemetry.meterBuilder(SCOPE)
            .setInstrumentationVersion("1.0.0")
            .build();
    }
}
