# framework Module — Tracing Changes

## 1. New tracing interfaces (4 new files)

### `TracingContextSpanBuilder.java` — Interface
Functional interface for creating spans and wrapping execution. Provides methods like `executeSupplierWithNewSpan(Supplier<T>, TracingSpanInformationSource)` that take a Java functional interface and manage the span lifecycle (start → make current → run logic → set attributes → end).

### `EventTracingContextExtractor.java` — Interface
Extracts trace context from events and restores it during processing. Method: `extractAndRestoreContextFromEvent(Event, Runnable)`.

### `TracingSpanInformationSource.java` — Interface (Mixin)
Provides class/method information for span naming. Methods: `getSpanClass()` and `getSpanMethod()`. Implemented by reflective handler classes so span names are human-readable (e.g., `CommandRouter.handle`).

### `EventTracingContextGetter.java` — OTel `TextMapGetter<Event>`
Implements OpenTelemetry's `TextMapGetter` interface to allow `propagator.extract()` to read `traceparent`/`tracestate` fields from an `Event` record.

---

## 2. OTel implementations (3 new files)

### `OpenTelemetryTracingContextSpanBuilder.java`
- Creates a span with `Tracer.spanBuilder(spanName)`, makes it current via `SpanContext`
- Runs the supplied `Supplier<T>` or `Runnable`
- Sets `handler.success = true` attribute on success, `handler.success = false` + exception on failure
- Ends the span in a `finally` block

### `NoTracingContextSpanBuilder.java`
- No-op: directly invokes the supplier/runnable without any span management

### `OpenTelemetryEventTracingContextExtractor.java`
- Uses `propagator.extract(context, event, new EventTracingContextGetter())` to restore OTel context from an event
- Calls `context.makeCurrent()` before running the handler logic

### `NoEventTracingContextExtractor.java`
- No-op: directly runs the runnable without context extraction

---

## 3. `CommandRouter.java` — Command handling wrapped in spans

- New constructor parameter: `TracingContextSpanBuilder spanBuilder`
- Entire command handling flow is now wrapped in `spanBuilder.executeSupplierWithNewSpan()`, using `createCommandHandlingSpanInfo()` for the span name
- Post-processing step adds `events.published` attribute (count of events published by the command)

---

## 4. `EventHandlingProcessor.java` — Event handling wrapped in spans

- New constructor parameters: `TracingContextSpanBuilder spanBuilder`, `EventTracingContextExtractor contextExtractor`
- Before processing each event batch, calls `contextExtractor.extractAndRestoreContextFromEvent()` to restore trace context from the first event in the trail
- Wraps processing in `spanBuilder.executeSupplierWithNewSpan()`

---

## 5. `Util.java` — State rebuilding wrapped in spans

- `applyUsingHandlers()` now accepts a `TracingContextSpanBuilder` parameter
- Each state rebuilding handler invocation is wrapped in its own span via `spanBuilder.executeSupplierWithNewSpan()`
- New helper: `createStateRebuildingSpanInformation(HandlerDefinition)` for span naming

---

## 6. `CommandEventCapturer.java` — Tracing passed through to state rebuilding

- New constructor parameter: `TracingContextSpanBuilder spanBuilder`
- Passed through to `Util.applyUsingHandlers()` so state rebuilding during command execution also creates spans

---

## 7. `EventUpcasters.java` — Trace headers preserved during upcasting

- When events are upcast, `traceParent` and `traceState` headers are carried forward to the upcasted event so trace continuity is maintained across version boundaries.

---

## 8. Tests

### `OpenTelemetryTracingContextSpanBuilderTest.java` (new, 4 tests)
- `shouldCreateSpanWithCorrectName` — verifies span name from information source
- `shouldSetSuccessAttributeOnCompletion` — verifies `handler.success = true`
- `shouldSetErrorAttributeOnException` — verifies error handling sets attributes
- `shouldMakeSpanCurrent` — verifies span is made current during execution

### `OpenTelemetryEventTracingContextExtractorTest.java` (new)
- Verifies trace context is extracted from events and restored as the current OTel context

### `EventTracingContextGetterTest.java` (new)
- Unit tests for the `TextMapGetter<Event>` implementation — verifies correct keys are returned and values read from event headers

### `CommandAndEventHandlingIntegrationTest.java` (updated)
- Added `MockServerContainer` for OpenTelemetry collector verification
- Tests verify that trace spans are properly created and exported during command + event handling flows

---

## 9. `application.yml` — Test config

- Disables OTel tracing/metrics export to avoid noise during unit tests
- Configures batch exporter with reasonable timeouts for test environment
