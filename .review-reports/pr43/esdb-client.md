# esdb-client Module — Tracing Changes

## 1. New package: `com.opencqrs.esdb.client.tracing` (4 new files)

### `TracingEventEnricher.java` — Interface
Single method: `EventCandidate enrichWithTracingData(EventCandidate candidate)`
Follows W3C Trace Context standard. Contract: return a new `EventCandidate` with tracing headers set, or the original if no-op.

### `OpenTelemetryTracingEventEnricher.java` — OTel implementation
- Reads `traceparent`/`tracestate` from the current OTel context via `TextMapPropagator.inject()`
- Preserves existing headers on the candidate if already set (doesn't overwrite)

### `NoTracingEventEnricher.java` — No-op identity implementation
Returns the candidate unchanged.

---

## 2. `EventCandidate.java` — Added two fields + convenience constructor

```java
// Before:
record EventCandidate(String source, String subject, String type, Map<String, ?> data)

// After:
record EventCandidate(String source, String subject, String type, Map<String, ?> data,
                      String traceParent, String traceState)

// Plus a backward-compatible convenience constructor that passes null,null
```

---

## 3. `Event.java` — Added two fields to the record

```java
// After:
record Event(..., String predecessorHash, String traceParent, String traceState)
```

These are the headers read back from the event store after events are persisted.

---

## 4. `EsdbClient.java` — Tracing enrichment wired into write path

- New constructor parameter: `TracingEventEnricher tracingEventEnricher`
- In `write()` method, before serialization: each `EventCandidate` is passed through the enricher

---

## 5. `JacksonMarshaller.java` — Serialization/deserialization of tracing headers

- **Write path**: New `toJackson(EventCandidate)` helper maps `traceParent`/`traceState` → Jackson's `JacksonEventCandidate.traceparent`/`tracestate`. Uses `@JsonInclude(NON_NULL)` so headers are omitted when absent.
- **Read path**: `fromResponseElement()` passes through `traceparent`/`tracestate` from the API response into the `Event` record.
- Inner records updated: `JacksonEventCandidate`, `JacksonResponseElement.Event` both gain the two fields.

---

## 6. `build.gradle.kts` — New dependency

- Added `compileOnly("io.opentelemetry:opentelemetry-api")` — the client module doesn't require OTel at runtime, but needs it for compilation when tracing is enabled.
- Test scope: added `spring-boot-starter-opentelemetry` for integration tests.

---

## 7. Tests

- **`OpenTelemetryTracingEventEnricherTest.java`** (new) — Two tests:
  - `shouldEnrichFromPropagatedTracingContext` — verifies headers are injected from a mocked propagator
  - `shouldKeepExistingTracingHeaders` — verifies existing headers on the candidate are preserved

- **`EsdbClientIntegrationTest.java`** — Updated to pass `traceParent`/`traceState` through write events, and asserts they round-trip correctly when read back. Comparison assertions ignore the new fields where auto-generated (id, time, hash, predecessorHash).

- **`application.yml`** — Test config disables OTel tracing/metrics export to avoid noise during tests.
