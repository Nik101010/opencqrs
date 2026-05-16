# esdb-client-spring-boot-autoconfigure Module — Tracing Changes

## 1. `OpenTelemetryEventEnricherAutoConfiguration.java` (new)
Creates the `TracingEventEnricher` bean when OpenTelemetry is on the classpath:
- `@Bean @ConditionalOnBean(OpenTelemetry.class) @ConditionalOnMissingBean(TracingEventEnricher.class)`
- Instantiates `OpenTelemetryTracingEventEnricher` with the injected `OpenTelemetry` instance
- The enricher uses OTel's `getPropagators()` to get the configured `TextMapPropagator`

## 2. `NoTracingEventEnricherAutoConfiguration.java` (new)
Creates a fallback no-op enricher:
- `@Bean @ConditionalOnMissingBean(TracingEventEnricher.class) @Order(Integer.MAX_VALUE)`
- Instantiates `NoTracingEventEnricher` (identity function)
- Lowest precedence (`@Order(Integer.MAX_VALUE)`) so the OTel bean takes priority when both are present
- Ensures a `TracingEventEnricher` is always available even without OpenTelemetry

## 3. `EsdbClientAutoConfiguration.java` — Tracing enricher wired in
- Constructor of the `esdbClient` bean now accepts `TracingEventEnricher tracingEventEnricher`
- Passed through to the `EsdbClient` constructor so write operations enrich events with trace headers

## 4. `AutoConfiguration.imports` — New auto-configurations registered
- Added entries for both new auto-configuration classes so Spring Boot discovers them automatically

---

## Design Notes
- **Opt-in pattern**: No tracing overhead unless `OpenTelemetry` bean is present (i.e., user added `spring-boot-starter-opentelemetry`)
- **Fallback guarantee**: Even without OTel, a `NoTracingEventEnricher` is auto-configured so the framework works without tracing
- **Precedence**: OTel enricher wins over no-op via `@ConditionalOnBean` + no-op uses `@Order(Integer.MAX_VALUE)`
