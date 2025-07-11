# IDORIS Event-Based Architecture

## Overview

This document provides an overview of the event-based architecture implemented in IDORIS using Spring Modulith. The
architecture is designed to support various use cases including PID record creation, versioning, callbacks for entity
changes, importing entities from existing DTRs, and schema generation.

## Architecture Components

### 1. Domain Events

Domain events represent significant occurrences within the system. They are used to communicate between modules in a
loosely coupled way. The following domain events have been implemented:

- **EntityCreatedEvent**: Published when a new entity is created
- **EntityUpdatedEvent**: Published when an entity is updated
- **EntityDeletedEvent**: Published when an entity is deleted
- **PIDGeneratedEvent**: Published when a PID is generated for an entity
- **SchemaGeneratedEvent**: Published when a schema is generated for an entity
- **EntityImportedEvent**: Published when an entity is imported from an external system
- **VersionCreatedEvent**: Published when a new version of an entity is created

### 2. Event Publisher

The `EventPublisherService` provides a centralized way to publish domain events. It wraps Spring's
`ApplicationEventPublisher` and provides a more domain-specific API.

### 3. Event Listeners

Event listeners subscribe to domain events and perform actions in response. The following listeners have been
implemented:

- **PIDGenerationEventListener**: Listens for entity creation events and generates PIDs
- **EntityChangeNotifier**: Listens for entity lifecycle events and notifies subscribers

### 4. Service Layer

The service layer encapsulates business logic and publishes domain events when entities are created, updated, or
deleted. The following services have been implemented:

- **TypeProfileService**: Manages TypeProfile entities
- **AtomicDataTypeService**: Manages AtomicDataType entities
- **OperationService**: Manages Operation entities

### 5. Notification System

The notification system allows external systems to subscribe to entity changes. It includes:

- **EntityChangeSubscriber**: Interface for subscribers that want to be notified of entity changes
- **EntityChangeNotifier**: Component that listens for entity change events and notifies subscribers
- **LoggingEntityChangeSubscriber**: Sample implementation that logs entity changes

## Module Structure

The application is organized into the following modules:

1. **Core Module**: Base abstractions, common interfaces, and event infrastructure
2. **Domain Module**: Entity definitions, domain services, and business logic
3. **Repository Module**: Data access objects and persistence
4. **Notification Module**: Callback mechanism for entity changes
5. **Web Module**: REST controllers and API

## Event Flow Examples

### PID Record Creation

1. An entity is created via a service method
2. The service publishes an `EntityCreatedEvent`
3. The `PIDGenerationEventListener` listens for this event
4. The listener generates a PID using the TypedPID-Maker
5. The listener publishes a `PIDGeneratedEvent`

### Entity Change Notification

1. An entity is updated via a service method
2. The service publishes an `EntityUpdatedEvent`
3. The `EntityChangeNotifier` listens for this event
4. The notifier calls all subscribers registered for that entity type or specific entity

## Implementation Details

### Publishing Events

```java
// In a service method
public TypeProfile createTypeProfile(TypeProfile typeProfile) {
    TypeProfile saved = typeProfileDao.save(typeProfile);
    eventPublisher.publishEntityCreated(saved);
    return saved;
}
```

### Listening for Events

```java
@Component
public class PIDGenerationEventListener {
    @EventListener
    @Transactional
    public void handleEntityCreatedEvent(EntityCreatedEvent<GenericIDORISEntity> event) {
        GenericIDORISEntity entity = event.getEntity();
        // Generate PID and update entity
        eventPublisher.publishPIDGenerated(entity, pid);
    }
}
```

### Subscribing to Entity Changes

```java
// Register a subscriber
entityChangeNotifier.subscribeToType("TypeProfile", mySubscriber);

// Implement the subscriber interface
public class MySubscriber implements EntityChangeSubscriber {
    @Override
    public void onEntityCreated(GenericIDORISEntity entity) {
        // Handle entity creation
    }
    
    @Override
    public void onEntityUpdated(GenericIDORISEntity entity, Long previousVersion) {
        // Handle entity update
    }
    
    @Override
    public void onEntityDeleted(GenericIDORISEntity entity) {
        // Handle entity deletion
    }
}
```

## Benefits

1. **Loose Coupling**: Modules communicate through events, reducing direct dependencies
2. **Extensibility**: New functionality can be added by implementing new event listeners
3. **Testability**: Components can be tested in isolation
4. **Scalability**: Event-based architecture can be scaled horizontally
5. **Maintainability**: Clear separation of concerns makes the codebase easier to understand and maintain

## Next Steps

1. **Implement Additional Event Listeners**: For schema generation, versioning, etc.
2. **Add Event Persistence**: Store events for audit and replay purposes
3. **Implement Event Sourcing**: Rebuild entity state from events
4. **Add Event Monitoring**: Monitor event flow and performance
5. **Implement Event Versioning**: Handle changes to event structure over time