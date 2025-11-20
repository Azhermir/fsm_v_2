# Task Management Service

A microservice for managing field service tasks in the FSM (Field Service Management) system.

## Overview

This Spring Boot microservice provides REST APIs for creating and managing service tasks. It follows a clean architecture with separate layers for entities, repositories, services, and controllers.

## Features

- Create service tasks with complete details
- Domain-driven design with ServiceTask aggregate
- RESTful API endpoints
- OpenAPI/Swagger documentation
- Comprehensive validation
- H2 in-memory database for testing
- PostgreSQL support for production

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Spring Data JPA**: For data persistence
- **PostgreSQL**: Production database
- **H2**: In-memory database for testing
- **Lombok**: To reduce boilerplate code
- **Springdoc OpenAPI**: API documentation
- **Maven**: Build tool
- **JUnit 5 & Mockito**: Testing frameworks

## Project Structure

```
task-management-svc/
├── src/
│   ├── main/
│   │   ├── java/com/fsm/taskmanagement/
│   │   │   ├── config/         # Configuration classes
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── entity/         # Domain entities
│   │   │   ├── repository/     # Data access layer
│   │   │   └── service/        # Business logic layer
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/com/fsm/taskmanagement/
│       │   ├── controller/     # Controller tests
│       │   ├── entity/         # Entity tests
│       │   ├── repository/     # Repository tests
│       │   └── service/        # Service tests
│       └── resources/
│           └── application.properties
└── pom.xml
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL (for production)

### Building the Application

```bash
mvn clean install
```

### Running Tests

```bash
mvn test
```

### Running the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### API Documentation

Once the application is running, you can access:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/api-docs

## API Endpoints

### Create Service Task

**POST** `/api/tasks`

Creates a new service task.

**Request Body:**
```json
{
  "title": "Fix HVAC System",
  "description": "Repair broken air conditioning unit",
  "clientAddress": "123 Main St, City, State",
  "priority": "HIGH",
  "estimatedDuration": 120
}
```

**Response:** `201 Created`
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Fix HVAC System",
  "description": "Repair broken air conditioning unit",
  "clientAddress": "123 Main St, City, State",
  "priority": "HIGH",
  "estimatedDuration": 120,
  "status": "UNASSIGNED",
  "createdAt": "2025-11-20T19:00:00",
  "updatedAt": "2025-11-20T19:00:00"
}
```

**Validation Rules:**
- `title`: Required, not blank
- `description`: Required, not blank
- `clientAddress`: Required, not blank
- `priority`: Required, must be one of: LOW, MEDIUM, HIGH, CRITICAL
- `estimatedDuration`: Optional, in minutes

**Error Response:** `400 Bad Request`
```json
{
  "status": 400,
  "error": "Validation failed",
  "message": "{title=Title is required}",
  "timestamp": "2025-11-20T19:00:00"
}
```

## Domain Model

### ServiceTask Entity

Represents a service task in the field service management system.

**Properties:**
- `id`: UUID (auto-generated)
- `title`: String (required)
- `description`: String (required)
- `clientAddress`: String (required)
- `priority`: Priority enum (required, default: MEDIUM)
- `estimatedDuration`: Integer (optional, in minutes)
- `status`: Status enum (required, default: UNASSIGNED)
- `createdAt`: LocalDateTime (auto-generated)
- `updatedAt`: LocalDateTime (auto-updated)

### Enums

**Priority:**
- LOW
- MEDIUM
- HIGH
- CRITICAL

**Status:**
- UNASSIGNED
- ASSIGNED
- IN_PROGRESS
- COMPLETED

## Database Configuration

### Development (H2)

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
```

### Production (PostgreSQL)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/fsm_db
spring.datasource.username=fsm_user
spring.datasource.password=fsm_password
spring.datasource.driver-class-name=org.postgresql.Driver
```

## Testing

The project includes comprehensive tests:

- **Entity Tests**: 22 tests for domain model validation
- **Repository Tests**: 16 integration tests for data access
- **Service Tests**: 14 unit tests for business logic
- **Controller Tests**: 16 integration tests for REST API

**Total**: 68 tests with high code coverage

Run tests with coverage report:
```bash
mvn clean test jacoco:report
```

View coverage report: `target/site/jacoco/index.html`

## Code Quality

- **JaCoCo** for code coverage (85%+ target)
- **Maven compiler** with Java 17
- **Lombok** for clean code
- **Spring Validation** for input validation

## Contributing

1. Follow the existing code structure
2. Write unit tests for new features
3. Ensure all tests pass before committing
4. Maintain test coverage above 85%
5. Use Lombok annotations to reduce boilerplate

## License

Copyright © 2025 FSM Team
