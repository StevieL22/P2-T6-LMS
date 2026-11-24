# P2-T6-LMS
2nd project for Revature paid training working on Notifications &amp; Reporting for project 2.

**Tech Stack**:
Backend: React, J25, Spring Boot(STRICTLY 4.1.2), Maven, Multiple prof(dev, test, stage, prod), Swagger, JWT, Spring Security, JUnit5, Mockito
Playwright, Postman, Thunder-client, STS IDE/ Intellij IDEA

**Frontend**: React with vite, JWT, Bootstrap/Material UI/React-Material UI/Tailwind CSS

**Should have**: utils/enums/dto, folow api naming conventions(end with plural: api/v1/users(specified operation/class))

**Data Structure**: tables should be in at least 3NF form, minimum 10 record in each table, single db for entire app(lms-db, create all tables/views/store-procedures
under this DB). Add audit columns in each table[created_at, created_by, modified_at, modified_by]



## Bootup

Run these commands from a Windows terminal.

### Prerequisites

- Node.js and npm for the frontend
- Java 17 or newer for the backend
- PostgreSQL if you want to run the application with PostgreSQL

### Frontend

```powershell
cd frontend
npm install
npm run dev
```

The Vite development server prints the local URL in the terminal, usually
`http://localhost:5173`.

### SQL / PostgreSQL

The project includes PostgreSQL support, but it does not currently contain
database migrations or a datasource configuration. Create the database once:

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

Place Flyway SQL migrations under
`backend/src/main/resources/db/migration` before using migration-based schema setup.

The backend also includes H2 for local development. If no PostgreSQL datasource
is configured, Spring Boot may use the embedded H2 database instead.

### Backend

Open a separate terminal and run:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The backend starts on `http://localhost:8080` by default.



