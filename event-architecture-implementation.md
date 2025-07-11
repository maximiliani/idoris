# IDORIS Event-Driven Architecture Implementation

## Overview

This document summarizes the implementation of an event-driven architecture in IDORIS using Spring Modulith. The
implementation follows the design outlined in the `event-architecture-design.md` document and provides a foundation for
the various use cases mentioned in the requirements.

## Implemented Components

### Core Event Infrastructure

1. **DomainEvent Interface**: Base interface for all domain events in the system.
    - File: `/src/main/java/edu/kit/datamanager/idoris/core/events/DomainEvent.java`

2. **AbstractDomainEvent Class**: Abstract base class that implements the DomainEvent interface and provides common
   functionality.
    - File: `/src/main/java/edu/kit/datamanager/idoris/core/events/AbstractDomainEvent.java`

3. **Entity Lifecycle Events**:
    - `EntityCreatedEvent`: Published when a new entity is created.
        - File: `/src/main/java/edu/kit/datamanager/idoris/core/events/EntityCreatedEvent.java`
    - `EntityUpdatedEvent`: Published when an entity is updated.
        - File: `/src/main/java/edu/kit/datamanager/idoris/core/events/EntityUpdatedEvent.java`
    - `PIDGeneratedEvent`: Published when a PID is generated for an entity.
        - File: `/src/main/java/edu/kit/datamanager/idoris/core/events/PIDGeneratedEvent.java`

4. **EventPublisherService**: Service for publishing domain events.
    - File: `/src/main/java/edu/kit/datamanager/idoris/core/events/EventPublisherService.java`

### Event Listeners

1. **PIDGenerationEventListener**: Listens for EntityCreatedEvent and generates PIDs for entities.
    - File: `/src/main/java/edu/kit/datamanager/idoris/pids/PIDGenerationEventListener.java`

### Spring Modulith Configuration

1. **ModulithConfig**: Configuration class for Spring Modulith.
    - File: `/src/main/java/edu/kit/datamanager/idoris/core/config/ModulithConfig.java`

2. **Module Definitions**:
    - Core Module: `/src/main/java/edu/kit/datamanager/idoris/core/package-info.java`
    - Domain Module: `/src/main/java/edu/kit/datamanager/idoris/domain/package-info.java`
    - PID Module: `/src/main/java/edu/kit/datamanager/idoris/pids/package-info.java`

### Dependencies

Added Spring Modulith dependencies to `build.gradle`:

```gradle
implementation "org.springframework.experimental:spring-modulith-starter:${springModulithVersion}"
implementation "org.springframework.experimental:spring-modulith-events:${springModulithVersion}"
testImplementation "org.springframework.experimental:spring-modulith-test:${springModulithVersion}"
```

## How It Works

1. **Entity Creation/Update**:
    - When an entity is created or updated, the service layer publishes an `EntityCreatedEvent` or `EntityUpdatedEvent`
      using the `EventPublisherService`.

2. **PID Generation**:
    - The `PIDGenerationEventListener` listens for `EntityCreatedEvent` and generates a PID for the entity if it doesn't
      already have one.
    - After generating a PID, it publishes a `PIDGeneratedEvent`.

3. **Event Propagation**:
    - Events are propagated across module boundaries using Spring Modulith's event externalization mechanism.
    - Event listeners can be executed in a transaction using the `@Transactional` annotation.

## Next Steps

### 1. Implement Additional Event Types

- `EntityDeletedEvent`: For entity deletion
- `SchemaGeneratedEvent`: For schema generation
- `EntityImportedEvent`: For entity import from DTRs

### 2. Implement Additional Event Listeners

- **Versioning Listener**: To maintain a history of entity changes
- **Notification Listener**: To send callbacks for entity changes
- **Schema Generation Listener**: To generate and validate schemas
- **Import Listener**: To handle entity import from DTRs

### 3. Integrate with Existing Services

- Modify existing service classes to publish events when entities are created, updated, or deleted.
- Update the TypedPIDMakerIDGenerator to work with the event-driven architecture.

### 4. Create Module-Specific Services

- **Versioning Service**: For managing entity versions
- **Notification Service**: For managing subscriptions and sending callbacks
- **Schema Service**: For generating and validating schemas
- **Import Service**: For importing entities from DTRs

### 5. Testing

- Write unit tests for event classes and listeners
- Write integration tests for event propagation across modules
- Use Spring Modulith's testing support to verify module boundaries

## Conclusion

The implemented event-driven architecture provides a solid foundation for the various use cases mentioned in the
requirements. It leverages Spring Modulith to create a modular, loosely coupled system that can evolve into
microservices in the future if needed. The event-based approach allows for asynchronous processing, better error
handling, and clearer separation of concerns.

By following the next steps outlined above, the architecture can be extended to support all the required functionality
while maintaining the modularity and loose coupling of the system.