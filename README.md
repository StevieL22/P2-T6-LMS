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

## Bootup

Run these commands from a Windows terminal.

### Prerequisites

- Node.js and npm for the frontend
- Java 25 for the backend
- PostgreSQL if you want to run the application with PostgreSQL

### Frontend

```powershell
cd frontend
npm install
npm run dev
```

The Next.js development server is available at `http://localhost:3000`.

### SQL / PostgreSQL

The project includes PostgreSQL support, but database migrations and a datasource configuration must be provided before using PostgreSQL. Create the database once:

```sql
psql -U postgres
CREATE DATABASE "lms-db";
\q
```

Configure the backend datasource with environment variables before starting it:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/lms-db"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="your_password"
$env:SPRING_JPA_HIBERNATE_DDL_AUTO="update"
```

Place Flyway SQL migrations under `backend/src/main/resources/db/migration` before using migration-based schema setup. H2 is also included for local development.

### Backend

Open a separate terminal and run:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The backend starts at `http://localhost:8080`. Swagger UI is available at `http://localhost:8080/swagger-ui/index.html`, and the health check at `http://localhost:8080/actuator/health`.

## Testing

Run backend tests with `cd backend` and `./mvnw clean test -Dspring.profiles.active=test`. From `frontend`, run `npm test` or `npm run test:coverage`; run Cypress with `npx cypress run` after starting the frontend.
