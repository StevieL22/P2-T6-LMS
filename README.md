# P2-T6-LMS

## Project Overview

Learning Management System (LMS) developed by Team 6 as Project 2 for Revature training.

The application follows a monolithic full-stack architecture with a Spring Boot backend and a React frontend.

## Project Modules

- Member 1 — Identity and Access Management
- Member 2 — Course Management
- Member 3 — Learning Content and Delivery
- Member 4 — Enrollment and Assessment
- Member 5 — Reviews and Feedback
- Member 6 — Notifications and Reporting

## Technology Stack

### Backend

- Java 25
- Spring Boot 4.1.2
- Maven
- Spring Data JPA
- Spring Security and JWT
- Swagger / OpenAPI
- JUnit 5 and Mockito
- JaCoCo
- Flyway
- Actuator and Prometheus
- Playwright

### Frontend

- React
- Vite
- TypeScript
- React Router
- Axios
- Jest
- Cypress
- React Testing Library
- Styling framework — pending team decision

### Database

- MySQL or PostgreSQL — final selection pending team confirmation
- H2 for automated testing

## Project Structure

```text
P2-T6-LMS/
├── backend/
├── frontend/
├── .gitignore
└── README.md
```

The backend follows a feature-based package structure.

The frontend follows Atomic Design principles using atoms, molecules, organisms, templates and pages.

## Backend Setup

Configure the required database environment variables before starting the application.

```bash
cd backend
./mvnw spring-boot:run
```

Backend URL: http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui/index.html

Health check: http://localhost:8080/actuator/health

## Frontend Setup

```bash
cd frontend
npm ci
npm run dev
```

Frontend URL: http://localhost:5173

The backend URL is configured using `VITE_API_BASE_URL`.

## Testing

### Backend

```bash
cd backend
./mvnw clean test -Dspring.profiles.active=test
```

### Frontend

```bash
cd frontend
npm test
npm run test:coverage
```

### End-to-End Testing

Start the frontend server before running Cypress.

```bash
npx cypress run
```

Target test coverage: above 95%.

## Application Profiles

- dev — Development
- test — Automated testing
- stage — Staging
- prod — Production

## Database Requirements

- Single shared relational database.
- Third Normal Form (3NF).
- Minimum 10 records per table.
- Audit columns: created_at, created_by, modified_at, modified_by.
- Soft deletion instead of hard deletion.
- Database migrations managed through Flyway.

## Development Standards

- Follow REST API naming conventions.
- Use plural resource names, such as `/api/v1/users`.
- Organize code using controllers, services, repositories, entities, DTOs, utilities and enums.
- Do not commit passwords or sensitive credentials.

## Git Branching Strategy

- main — Stable releases
- develop — Shared development
- feature-m1 through feature-m6 — Individual module development

All feature branches should be reviewed before merging into develop.
