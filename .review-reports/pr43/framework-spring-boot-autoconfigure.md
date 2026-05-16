# framework-spring-boot-autoconfigure Module — Tracing Changes

## 1. `EventHandlingProcessorAutoConfiguration.java` — New tracing beans + wiring

### New beans added:
- `OpenTelemetryEventTracingContextExtractor` — created when `OpenTelemetry.class` is present, uses OTel propagator to extract trace context from events
- `NoEventTracingContextExtractor` — no-op fallback, created only if no other extractor exists
- `OpenTelemetryTracingContextSpanBuilder` — creates OTel spans for command/event handling when OpenTelemetry is present
- `NoTracingContextSpanBuilder` — no-op fallback for when OpenTelemetry is absent

### Wiring changes:
- `EventHandlingProcessor` bean creation now receives both `TracingContextSpanBuilder` and `EventTracingContextExtractor` as constructor parameters
- Same opt-in/fallback pattern: OTel beans take priority, no-op beans fill in when OTel is absent

## 2. `CommandRouterAutoConfiguration.java` — Tracing span builder wired in

- `CommandRouter` bean constructor now accepts `TracingContextSpanBuilder spanBuilder`
- Entire command handling flow is wrapped in spans via this builder

## 3. `CommandHandlingAnnotationProcessingAutoConfiguration.java` — Span naming for command handlers

- `ReflectiveMethodInvocationCommandHandler` now implements `TracingSpanInformationSource`
- Provides `getSpanClass()` (handler method class name) and `getSpanMethod()` (method signature)
- Enables human-readable span names like `CommandHandler.handle(CreateOrderCommand)`

## 4. `StateRebuildingAnnotationProcessingAutoConfiguration.java` — Span naming for state rebuilding handlers

- `ReflectiveMethodInvocationStateRebuildingHandler` now implements `TracingSpanInformationSource`
- Same pattern: provides class name and method signature for span naming

## 5. `EventHandlingAnnotationProcessingAutoConfiguration.java` — Span naming for event handlers

- `ReflectiveMethodInvocationEventHandler` now implements `TracingSpanInformationSource`
- Enables span names like `OrderCreatedHandler.handle(OrderCreatedEvent)`

---

## Design Notes
- **Consistent pattern**: All three handler types (command, event, state rebuilding) implement `TracingSpanInformationSource` for consistent span naming
- **Opt-in OTel**: All tracing beans use `@ConditionalOnBean(OpenTelemetry.class)` / `@ConditionalOnMissingBean` — zero overhead when tracing is disabled
- **Mixin interface**: `TracingSpanInformationSource` is a lightweight interface that handler classes can implement without changing their core behavior
