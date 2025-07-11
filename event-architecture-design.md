# IDORIS Event-Driven Architecture Design

## Introduction

This document outlines the design for implementing an event-driven architecture in IDORIS using Spring Modulith. The
design is inspired by the approach used
in [piomin/sample-spring-modulith](https://github.com/piomin/sample-spring-modulith) and aims to support various use
cases including PID record creation, versioning, callbacks for entity changes, importing entities from existing DTRs,
and schema generation.

## Goals

- Implement a loosely coupled, event-driven architecture
- Support cross-module communication through domain events
- Enable asynchronous processing of business operations
- Provide a foundation for future microservices extraction
- Support specific use cases mentioned in the requirements

## Event-Driven Architecture Overview

The event-driven architecture will be based on Spring Modulith's ApplicationModuleListener mechanism, which provides:

1. **Module Boundaries**: Clear separation between modules
2. **Event Publication**: Standardized way to publish domain events
3. **Event Subscription**: Type-safe event handling across module boundaries
4. **Transaction Management**: Events can be processed in the same or separate transactions

## Domain Events

We will define a hierarchy of domain events:

```
DomainEvent (base interface)
├── EntityCreatedEvent<T extends GenericIDORISEntity>
├── EntityUpdatedEvent<T extends GenericIDORISEntity>
├── EntityDeletedEvent<T extends GenericIDORISEntity>
├── PIDGeneratedEvent<T extends GenericIDORISEntity>
├── SchemaGeneratedEvent
└── EntityImportedEvent<T extends GenericIDORISEntity>
```

Each event will contain relevant data and metadata about the operation that triggered it.

## Module Structure

The event-driven architecture will be organized around the following modules:

1. **Core Module**
    - Event definitions
    - Common interfaces
    - Base abstractions

2. **Domain Module**
    - Entity definitions
    - Domain services
    - Domain event publishers

3. **PID Module**
    - PID generation services
    - PID record management
    - Event listeners for entity lifecycle events

4. **Versioning Module**
    - Version tracking
    - Change history
    - Event listeners for entity updates

5. **Schema Module**
    - Schema generation
    - Schema validation
    - Event listeners for schema-related events

6. **Import Module**
    - Entity import services
    - DTR connectors
    - Event listeners for import-related events

7. **Notification Module**
    - Callback management
    - Subscription services
    - Event listeners for entity changes

## Event Flow Examples

### PID Record Creation

1. An entity is created or updated in the Domain Module
2. The Domain Module publishes an EntityCreatedEvent or EntityUpdatedEvent
3. The PID Module listens for these events
4. The PID Module generates a PID using TypedPID-Maker
5. The PID Module publishes a PIDGeneratedEvent
6. Other modules can react to the PIDGeneratedEvent

### Entity Versioning

1. An entity is updated in the Domain Module
2. The Domain Module publishes an EntityUpdatedEvent
3. The Versioning Module listens for this event
4. The Versioning Module creates a new version record
5. The Versioning Module publishes a VersionCreatedEvent

### Callbacks for Entity Changes

1. A client subscribes to changes for a specific entity type
2. The entity is modified in the Domain Module
3. The Domain Module publishes an EntityUpdatedEvent
4. The Notification Module listens for this event
5. The Notification Module checks for subscriptions
6. The Notification Module sends callbacks to subscribers

## Implementation Approach

### 1. Spring Modulith Setup

Add Spring Modulith dependencies to the project:

```gradle
implementation 'org.springframework.experimental:spring-modulith-starter:1.1.0'
implementation 'org.springframework.experimental:spring-modulith-events:1.1.0'
testImplementation 'org.springframework.experimental:spring-modulith-test:1.1.0'
```

### 2. Domain Event Definitions

Create base domain event interfaces and implementations:

```java
public interface DomainEvent {
    Instant getTimestamp();
    String getEventId();
}

public abstract class AbstractDomainEvent implements DomainEvent {
    private final Instant timestamp = Instant.now();
    private final String eventId = UUID.randomUUID().toString();
    
    @Override
    public Instant getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String getEventId() {
        return eventId;
    }
}

public class EntityCreatedEvent<T extends GenericIDORISEntity> extends AbstractDomainEvent {
    private final T entity;
    
    public EntityCreatedEvent(T entity) {
        this.entity = entity;
    }
    
    public T getEntity() {
        return entity;
    }
}
```

### 3. Event Publishers

Implement event publishers in the domain services:

```java
@Service
public class TypeProfileService {
    private final ApplicationEventPublisher eventPublisher;
    private final TypeProfileRepository repository;
    
    @Autowired
    public TypeProfileService(ApplicationEventPublisher eventPublisher, TypeProfileRepository repository) {
        this.eventPublisher = eventPublisher;
        this.repository = repository;
    }
    
    @Transactional
    public TypeProfile createTypeProfile(TypeProfile typeProfile) {
        TypeProfile saved = repository.save(typeProfile);
        eventPublisher.publishEvent(new EntityCreatedEvent<>(saved));
        return saved;
    }
    
    @Transactional
    public TypeProfile updateTypeProfile(TypeProfile typeProfile) {
        TypeProfile saved = repository.save(typeProfile);
        eventPublisher.publishEvent(new EntityUpdatedEvent<>(saved));
        return saved;
    }
}
```

### 4. Event Listeners

Implement event listeners in the appropriate modules:

```java
@Component
public class PIDGenerationEventListener {
    private final TypedPIDMakerIDGenerator pidGenerator;
    private final ApplicationEventPublisher eventPublisher;
    
    @Autowired
    public PIDGenerationEventListener(TypedPIDMakerIDGenerator pidGenerator, ApplicationEventPublisher eventPublisher) {
        this.pidGenerator = pidGenerator;
        this.eventPublisher = eventPublisher;
    }
    
    @EventListener
    @Transactional
    public void handleEntityCreatedEvent(EntityCreatedEvent<GenericIDORISEntity> event) {
        GenericIDORISEntity entity = event.getEntity();
        if (entity.getPid() == null || entity.getPid().isEmpty()) {
            String pid = pidGenerator.generateId(entity.getClass().getSimpleName(), entity);
            entity.setPid(pid);
            eventPublisher.publishEvent(new PIDGeneratedEvent<>(entity, pid));
        }
    }
}
```

### 5. Transaction Management

Configure transaction boundaries for event processing:

```java
@Configuration
public class EventConfig {
    @Bean
    public TransactionalApplicationListener.Factory transactionalApplicationListenerFactory(
            TransactionManager transactionManager) {
        return TransactionalApplicationListener.factory(transactionManager);
    }
}
```

## Use Case Implementation Details

### PID Record Creation

The PID Module will listen for entity lifecycle events and use the TypedPIDMakerIDGenerator to create or update PID
records. This decouples PID generation from entity creation and allows for retry mechanisms and better error handling.

### Versioning

The Versioning Module will maintain a history of entity changes by listening for EntityUpdatedEvents. It will store
previous versions of entities and provide APIs to retrieve and compare versions.

### Callbacks for Entity Changes

The Notification Module will allow clients to subscribe to entity changes and receive callbacks when those entities are
modified. It will maintain a registry of subscriptions and use event listeners to trigger notifications.

### Importing Entities from DTRs

The Import Module will provide services to import entities from external Digital Twin Registries. It will publish
EntityImportedEvents when entities are imported, allowing other modules to react accordingly.

### Schema Generation

The Schema Module will generate and validate schemas for entities. It will listen for entity lifecycle events and
generate or update schemas as needed. It will publish SchemaGeneratedEvents when schemas are created or updated.

## Testing Strategy

1. **Unit Tests**: Test individual components within modules
2. **Module Tests**: Test modules in isolation using Spring Modulith test support
3. **Integration Tests**: Test interactions between modules through events
4. **End-to-End Tests**: Verify complete workflows involving multiple modules

## Implementation Phases

1. **Phase 1**: Set up Spring Modulith and implement base event infrastructure
2. **Phase 2**: Implement PID record creation and versioning
3. **Phase 3**: Implement callbacks for entity changes
4. **Phase 4**: Implement entity import from DTRs
5. **Phase 5**: Implement schema generation

## Conclusion

This event-driven architecture design provides a solid foundation for implementing the required functionality in IDORIS.
It leverages Spring Modulith to create a modular, loosely coupled system that can evolve into microservices in the
future if needed. The event-based approach allows for asynchronous processing, better error handling, and clearer
separation of concerns.