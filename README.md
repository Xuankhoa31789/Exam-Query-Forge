# Exam Query Forge

Exam Query Forge is a shared question-pool app for verified teachers, collaborative
review, voting, and matrix-based exam assembly.

The current `main` branch contains a basic Maven + Spring Boot application with:

- Static HTML/CSS/JavaScript in `src/main/resources/static`
- JSON REST APIs under `/api`
- Login and register controllers under `/api/auth`

## Run

```powershell
.\mvn-local.ps1 spring-boot:run
```

Then open:

```text
  http://localhost:8080
```

If Maven is installed globally later, this also works:

```powershell
mvn spring-boot:run
```

## API

Register:

```http
POST /api/auth/register
Content-Type: application/json
```

```json
{
  "name": "Demo User",
  "email": "demo@example.com",
  "password": "password123"
}
```

Login:

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "demo@example.com",
  "password": "password123"
}
```

## What this project is

Exam Query Forge (EQF) is a web app for verified teachers to manage exam
questions collaboratively. The core idea is to replace traditional random exam
assembly with a quality-controlled pipeline.

Flow:

1. Verified teachers contribute questions to a shared pool tagged by subject,
   chapter, and difficulty.
2. For an exam, a department head defines an exam matrix, such as 50 questions:
   80% recognition and low-application, 20% high-application.
3. The system pulls a wide candidate pool per difficulty bucket.
4. Subject teachers vote on candidates: +1 use, 0 needs-fix, -1 reject.
   Comments are required for 0 and -1.
5. The system selects the top-voted questions per bucket until the matrix is filled.
6. The department head finalizes the exam; only they see the final question set.

Security principle: teachers vote on a wide candidate pool but never learn which
questions made the final exam. This makes leaks low-value by design.

## Tech stack

- Java 17, Spring Boot, Maven
- Spring Data JPA + Hibernate
- Dev DB: H2 file-based database
- Prod DB target: PostgreSQL
- Frontend: static HTML/CSS/JS, with a possible later move to Thymeleaf or React

## Conventions

- Base package: `com.eqf`
- Layout: `model/`, `repository/`, `service/`, `controller/`, `config/`,
  `exception/`
- Store enums as strings with `@Enumerated(EnumType.STRING)`.
- Use Hibernate timestamps through `@CreationTimestamp` and `@UpdateTimestamp`.
- Never store plaintext passwords. Use BCrypt and never log raw passwords.
- Keep `src/main/resources/db/schema.postgres.sql` as the reference Postgres
  schema when present. It should not be auto-run against H2.

## Build approach: vertical slices

Build one feature end-to-end: entity, repository, service, controller, and view.
Keep each slice small and runnable before moving on.

Slice order:

1. Auth & User
2. Question bank
3. AI difficulty analysis
4. Exams, matrix, and candidate pulling
5. Voting, selection, and finalization

## Working notes

- Prefer the solution first, followed by a short explanation of each file.
- After changes, run `mvn spring-boot:run` or `mvn compile` and fix any errors.
- Keep slices small and confirm before large refactors.
- Bilingual Vietnamese / English notes are fine.
