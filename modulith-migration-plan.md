# IDORIS Spring Modulith Migration Plan

## Introduction

This document outlines the plan to transform IDORIS into a Spring Modulith application, replacing Spring Data REST with
Spring MVC and Spring HATEOAS for the API. The goal is to enhance maintainability, lower coupling, and increase
development freedom through a well-structured modular architecture.

## Goals and Benefits

- **Improved Maintainability**: Clear module boundaries make the codebase easier to understand and maintain
- **Reduced Coupling**: Explicit dependencies between modules prevent unwanted coupling
- **Enhanced Development Freedom**: Teams can work on separate modules with minimal interference
- **Better Testability**: Modules can be tested in isolation
- **Clearer Architecture**: Module boundaries reflect domain concepts

## Current Architecture Analysis

IDORIS currently uses:

- Spring Boot as the application framework
- Spring Data Neo4j for database access
- Spring Data REST for exposing repositories as REST APIs
- Spring HATEOAS for hypermedia support

The application is structured around these main components:

- Domain entities (AtomicDataType, TypeProfile, Operation, etc.)
- Data access objects (DAOs)
- Rules system (validation, processing)
- REST controllers and configuration

## Module Boundaries

Based on domain-driven design principles and the current codebase, we propose the following modules:

1. **Core Module**
    - Base abstractions and shared utilities
    - Common interfaces and base classes
    - Cross-cutting concerns

2. **Domain Module**
    - Entity definitions (DataType, TypeProfile, Operation, etc.)
    - Domain services and business logic
    - Domain events

3. **Rules Module**
    - Rule definitions and processing
    - Validation logic
    - Rule execution engine

4. **Repository Module**
    - Data access objects
    - Neo4j configuration
    - Query definitions

5. **Web Module**
    - REST controllers
    - Request/response DTOs
    - API documentation

## Package Structure

The new package structure will follow Spring Modulith conventions:

```
edu.kit.datamanager.idoris
├── core
│   ├── config
│   ├── exception
│   └── util
├── domain
│   ├── entities
│   ├── enums
│   ├── events
│   └── services
├── rules
│   ├── api
│   ├── logic
│   ├── processor
│   └── validation
├── repository
│   ├── config
│   ├── dao
│   └── mapping
└── web
    ├── api
    ├── controller
    ├── dto
    └── hateoas
```

## Migration from Spring Data REST to Spring MVC with HATEOAS

### Current Implementation

Spring Data REST automatically exposes repositories as REST endpoints with hypermedia support. This approach has
limitations:

- Limited control over API design
- Tight coupling between domain model and API representation
- Challenges with complex business logic

### New Implementation

1. **Define Controller Layer**
    - Create dedicated controllers for each resource type
    - Implement CRUD operations using Spring MVC
    - Use Spring HATEOAS for hypermedia support

2. **Create Resource Representations**
    - Define DTOs for request/response
    - Implement assemblers to convert between entities and DTOs
    - Add hypermedia links using LinkBuilder

3. **Implement Business Logic**
    - Move business logic from repositories to service classes
    - Ensure proper separation of concerns
    - Implement validation in appropriate layers

## Required Dependencies

Add the following dependencies to the build.gradle file:

```gradle
// Spring Modulith
implementation 'org.springframework.experimental:spring-modulith-starter:1.1.0'
testImplementation 'org.springframework.experimental:spring-modulith-test:1.1.0'

// Already present, keep these
implementation 'org.springframework.boot:spring-boot-starter-web'
implementation 'org.springframework.boot:spring-boot-starter-hateoas'
implementation 'org.springframework.boot:spring-boot-starter-validation'

// Remove this dependency
// implementation 'org.springframework.boot:spring-boot-starter-data-rest'
```

## Implementation Approach

### Phase 1: Setup Spring Modulith

1. Add Spring Modulith dependencies
2. Create the new package structure
3. Configure module boundaries
4. Write module documentation

### Phase 2: Migrate Domain Model

1. Reorganize domain entities into the new structure
2. Refactor domain services
3. Implement domain events for cross-module communication

### Phase 3: Implement Repository Layer

1. Migrate DAOs to the repository module
2. Refactor Neo4j configuration
3. Implement repository services

### Phase 4: Develop Web API

1. Create controllers for each resource type
2. Implement DTOs and assemblers
3. Add hypermedia support using Spring HATEOAS
4. Migrate from Spring Data REST endpoints

### Phase 5: Rules System Migration

1. Reorganize rules components
2. Implement clean interfaces between modules
3. Ensure rule execution works across module boundaries

### Phase 6: Testing and Validation

1. Write module tests using Spring Modulith test support
2. Verify module boundaries and dependencies
3. Test API endpoints
4. Validate hypermedia functionality

## Testing Strategy

1. **Unit Tests**: Test individual components within modules
2. **Module Tests**: Test modules in isolation using Spring Modulith test support
3. **Integration Tests**: Test interactions between modules
4. **API Tests**: Verify REST endpoints and hypermedia functionality

## Timeline

- **Week 1-2**: Setup Spring Modulith and restructure packages
- **Week 3-4**: Migrate domain model and repository layer
- **Week 5-6**: Implement web API with Spring MVC and HATEOAS
- **Week 7-8**: Migrate rules system and testing

## Conclusion

Transforming IDORIS into a Spring Modulith application with Spring MVC and HATEOAS will provide significant benefits in
terms of maintainability, coupling, and development freedom. The migration can be done incrementally, ensuring that the
application remains functional throughout the process.

The modular architecture will make it easier to understand the system, develop new features, and maintain the codebase
over time. The explicit module boundaries will prevent unwanted dependencies and ensure that the architecture remains
clean as the application evolves.