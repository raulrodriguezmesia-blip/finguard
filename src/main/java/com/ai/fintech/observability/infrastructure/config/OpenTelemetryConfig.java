package com.ai.fintech.observability.infrastructure.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenTelemetry para exportar traces via OTLP.
 * Configura el exportador OTLP y el proveedor de trazado.
 */
@Configuration
public class OpenTelemetryConfig {

    private static final String SERVICE_NAME = "ai-fintech-observability-platform";
    private static final String SERVICE_VERSION = "1.0.0";

    @Value("${OTEL_EXPORTER_OTLP_ENDPOINT:http://localhost:4317}")
    private String otlpEndpoint;

    @Bean
    public OpenTelemetry openTelemetry() {
        // Configurar el recurso con información del servicio
        Resource resource = Resource.getDefault()
                .merge(Resource.create(Attributes.of(
                        AttributeKey.stringKey("service.name"), SERVICE_NAME,
                        AttributeKey.stringKey("service.version"), SERVICE_VERSION
                )));

        // Configurar el exportador OTLP
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(otlpEndpoint)
                .build();

        // Configurar el proveedor de trazado
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setResource(resource)
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .build();

        // Construir y retornar el SDK de OpenTelemetry
        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(SERVICE_NAME, SERVICE_VERSION);
    }
}
