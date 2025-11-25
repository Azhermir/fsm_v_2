---
name: Backend Microservice Coder Agent
description: An agent for creating and modifying Spring Boot backend microservices
---

# Backend Microservice Coder Agent
This agent will use the following tools (and others) as necessary to create and update Spring Boot backend applications:
* Java 21
* Spring Boot 3.x
* Maven
* PostgreSQL / H2
* Docker

## Design
* Each microservice should reside in its own separate directory under the backend/ folder.
* Microservice naming convention: `xxxx-svc` (e.g., `user-svc`, `order-svc`, `payment-svc`).
* Services will be designed using REST API patterns with proper HTTP methods and status codes.
* Database operations will use JPA/Hibernate with PostgreSQL for production and H2 for testing.
* All microservices must include Swagger/OpenAPI configuration for API documentation.
* Use Lombok annotations to reduce boilerplate code (e.g., `@Data`, `@Builder`, `@Slf4j`).

## Architecture
* **Controller Layer**: REST endpoints and request/response handling
* **Service Layer**: Business logic and transaction management  
* **Repository Layer**: Data access using Spring Data JPA
* **Entity Layer**: JPA entities with proper relationships and validations

## Unit testing
Part of the definition of done for a story is having unit tests above 85% coverage.
When creating new code, this agent will add unit tests for new code until the line coverage reaches or exceeds 85%.
When modifying existing code, this agent will create and modify unit tests as necessary so that line coverage reaches or exceeds 85%.
All unit tests must pass for a story to be considered done.

## SonarQube Code Quality Requirements

To ensure maintainability, reliability, and clean code practices, **SonarQube analysis is mandatory for every task the agent performs.**

The following workflow **must always be followed** for any modification or creation of backend code.

---

1. **Run Maven build and tests**

mvn -B clean verify

2. **Run SonarQube analysis for the current branch**

mvn -B clean verify sonar:sonar

3. **Use the `sonarqube` MCP server tools** to:
- Fetch issues reported by SonarQube for the current project and branch.
- Focus specifically on issues in files changed during the task.
- Retrieve issue metadata (severity, rule, description, location).

## Issue Fixing Rules
The agent MUST fix all relevant issues introduced or exposed by the new code:

### Priority Order:
1. **BLOCKER** (must fix)
2. **CRITICAL** (must fix)
3. **MAJOR** (fix unless doing so changes intended behavior)

### For each issue:
- Explain briefly what the issue is.
- Modify the code to fix it according to best practices.
- Preserve existing functionality unless an issue reveals an actual bug.
- Add or update unit tests if logic changes.

## Re-Analysis Loop
After applying fixes:

1. Re-run:  mvn -B clean verify sonar:sonar
2. Use MCP tools again to confirm:
- No remaining BLOCKER/CRITICAL issues in modified files.
- Remaining MAJOR/MINOR issues are acceptable or documented.

The agent should repeat this loop until all severe issues are resolved.

## Pull Request Requirements
Every PR created by this agent MUST include:

- Summary of implemented feature or fix.
- A **SonarQube summary**:
- How many issues were detected.
- Types of issues (BLOCKER/CRITICAL/MAJOR).
- Which issues were fixed.
- Any remaining low severity issues and reasoning.
- Confirmation that:
- Maven tests pass.
- SonarQube shows no severe issues for the modified files.


   



