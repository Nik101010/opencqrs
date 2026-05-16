# example-application Module — Tracing Changes

## 1. `docker-compose.yml` — Added tracing infrastructure services

- **Zipkin** service — Distributed trace aggregator with web UI (default port 9411). Provides a visual interface for exploring traces, viewing span timelines, and debugging latency issues.
- **OTel Collector** service — Receives OTLP telemetry from the application, processes it, and exports to downstream backends (Zipkin + debug/console).

Application container now depends on both services, ensuring tracing infrastructure is available before the app starts.

## 2. `otel-collector-config.yaml` (new) — OTel Collector pipeline configuration

```yaml
receivers:
  otlp:
    protocols:
      http:                    # OTLP over HTTP on port 4318

exporters:
  zipkin:                    # Export traces to Zipkin
    endpoint: http://zipkin:9411/api/v2/spans
  logging:                   # Debug output to console (shows received spans)

service:
  pipelines:
    traces:
      receivers: [otlp]
      exporters: [zipkin, logging]
```

- Receives traces via OTLP HTTP (port 4318)
- Exports to both Zipkin for visualization and logging for debugging
- The `logging` exporter is useful during development to see raw span data in console output

## 3. `application.yml` — OpenTelemetry configuration

- Configured `management.tracing.sampling.probability: 1.0` (100% sampling for demo/development)
- Set `management.otlp.tracing.endpoint` to point at the OTel collector (`http://otel-collector:4318`)
- Configures Spring Boot Actuator's built-in OpenTelemetry support to automatically create spans for HTTP requests, command handling, etc.

## 4. `build.gradle.kts` — OTel dependency added

- Added `runtimeOnly("org.springframework.boot:spring-boot-starter-opentelemetry")` — Spring Boot's starter auto-configures OTel Tracer, propagator, and Micrometer integration
- `runtimeOnly` (not `implementation`) because the framework itself doesn't depend on OTel — only the example app needs it at runtime

---

## Design Notes
- The example application serves as a reference implementation showing how to wire up distributed tracing with the framework's opt-in tracing architecture
- Zipkin + OTel Collector provides a complete end-to-end tracing demo: application → OTel collector → Zipkin
- 100% sampling is appropriate for development/demo; production would typically use a lower probability
