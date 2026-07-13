# AGENTS.md — Exam Query Forge (EQF)

> Codex reads this file automatically before working. It is the source of truth for
> project context, conventions, current status, and prompt patterns. Keep it updated.
> The human developer communicates in Vietnamese (mixes English for technical terms).

## What this project is

EQF is a web app **for verified teachers** to build exams collaboratively. Core idea:
replace the traditional random-draw ("bốc thăm") method with a quality-controlled flow.

1. Verified teachers contribute questions to a **shared pool** (tagged by subject,
   chapter, difficulty).
2. A department head defines an **exam matrix** (e.g. 50 questions: 80% recognition +
   low-application, 20% high-application).
3. The system pulls a **wide candidate pool** per difficulty bucket.
4. Subject teachers **vote** on candidates (+1 use / 0 needs-fix / -1 reject).
5. The system selects top-voted questions per bucket until the matrix is filled.
6. The department head finalizes. Teachers never learn which questions made the final
   exam — this makes leaks low-value by design.

## Tech stack

- Java 17, Spring Boot 3.3.5, Maven
- Spring Data JPA + Hibernate
- **Dev DB: H2 (file-based)** at `./data/exam-query-forge`; console at `/h2-console`
  (JDBC URL `jdbc:h2:file:./data/exam-query-forge`, user `sa`, no password).
  **Prod DB: PostgreSQL** via Spring profile `prod` (`application-prod.properties`);
  entities are DB-agnostic, no code changes needed. See "Deploy" section below.
- Spring Security + **JWT (JJWT 0.12, HS256)**: stateless Bearer tokens, BCrypt hashing.
- Frontend: static HTML/CSS/JS in `src/main/resources/static/`

## Build & run

- Run: `mvn spring-boot:run`  → app at `http://localhost:8080`
- Login/register page: `/index.html`. Question bank: `/questions.html`.
- **Restart the app after adding/renaming any Java file** — new endpoints won't exist
  until you restart (a 404 on a new endpoint usually means "you forgot to restart").

## Conventions

- Base package `com.eqf`; layout: `model/` (entities+enums), `repository/`, `service/`,
  `controller/`, `config/`, `dto/`, `exception/`.
- Enums stored as **VARCHAR via `@Enumerated(EnumType.STRING)`** (not native PG enums).
- Timestamps via `LocalDateTime` + `@PrePersist`/`@PreUpdate`.
- Passwords: **never plaintext, always BCrypt.** Never log raw passwords.
- `src/main/resources/db/schema.postgres.sql` is a **reference only**. It is
  Postgres-specific and must NOT run on H2. Keep `spring.sql.init.mode=never`.

## Data model (13 tables; see schema.postgres.sql)

subjects, users, credentials · chapters, questions, answer_options, ai_analyses ·
exams, exam_matrix, exam_candidates, votes · audit_log, question_usage_history

Key points:
- `votes` reference `exam_candidates` (per-exam context), NOT raw `questions`.
- `ai_analyses` holds AI difficulty **suggestions + reasoning only**; the human-set
  `questions.difficulty` is authoritative. AI never overrides it.

## Vertical-slice plan & CURRENT STATUS

Build one feature end-to-end (entity → repo → service → controller → view) until it
runs, then move on. Do NOT build all entities, then all repos.

- [x] **Slice 1 — Auth & User.** Subject/User/Credential, BCrypt, email login.
- [x] **Slice 2 — Question bank.** Chapter/Question/AnswerOption + CRUD + `questions.html` UI.
      Only VERIFIED users may author (enforced in QuestionService).
- [x] **Slice 3 — AI difficulty (stub).** `DifficultyAnalyzer` interface +
      `StubDifficultyAnalyzer` (heuristic). Endpoint `/api/ai/analyze`. In the form,
      pressing **Tab** in the content box auto-fills the difficulty dropdown.
      Real LLM = nhịp 2, pending an API key: add a new `DifficultyAnalyzer` impl marked
      `@Primary` — do not touch the service/controller/UI.
- [x] Login/logout flow fixed: login stored in `sessionStorage('eqfCurrentUser')`,
      shared across index.html, questions.html, and exams.html; logout redirects to `/index.html`.
- [x] **Slice 4 — Exams + matrix + candidate pulling.** Backend REST + `exams.html` UI complete.
- [x] **Slice 5 — Voting + selection + finalize.** `voting.html`, VoteService,
      select-by-score + finalize (only the exam creator), usage history for cooldown.
- [x] **Slice 6 — Real JWT auth.** Package `com.eqf.security` (JwtService, JwtAuthFilter,
      AuthenticatedUser). Login returns a real JWT plus `userId`, `fullName`, `role`.
      Every `/api/**` except login/register/health requires `Authorization: Bearer <token>`
      (401 otherwise). authorId/createdById/voterId come from the JWT, NOT request bodies.
      Frontend: shared `static/auth.js` — `apiFetch()` attaches the header; 401 → login page.
- [x] **Slice 7 — Production PostgreSQL.** postgresql driver (runtime),
      `application-prod.properties` (all secrets from env), multi-stage `Dockerfile`,
      `DevDataInitializer` restricted to profile default/dev.
- [ ] (Later) role enforcement per endpoint (e.g. only DEPARTMENT_HEAD creates/finalizes
      exams); audit_log slice; real LLM DifficultyAnalyzer (pending API key).

## Deploy (Slice 7)

Prod = Spring profile `prod`. Required env vars:

| Env var | Meaning |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `DATABASE_URL` | JDBC URL: `jdbc:postgresql://host:5432/dbname`. If your platform only provides `postgres://user:pass@host/db`, set `SPRING_DATASOURCE_URL` + `SPRING_DATASOURCE_USERNAME` + `SPRING_DATASOURCE_PASSWORD` instead. |
| `EQF_JWT_SECRET` | JWT signing secret, **>= 32 bytes** (app refuses to start otherwise) |
| `PORT` | optional; Render sets it automatically (default 8080) |

Run prod locally against a local Postgres (PowerShell):

```powershell
$env:SPRING_PROFILES_ACTIVE = 'prod'
$env:DATABASE_URL = 'jdbc:postgresql://localhost:5432/eqf'
$env:SPRING_DATASOURCE_USERNAME = 'postgres'
$env:SPRING_DATASOURCE_PASSWORD = '<your-password>'
$env:EQF_JWT_SECRET = 'some-long-random-secret-at-least-32-bytes!!'
mvn spring-boot:run
```

Docker (what Render builds from the `Dockerfile`):

```bash
docker build -t eqf .
docker run -p 8080:8080 -e DATABASE_URL=... -e EQF_JWT_SECRET=... eqf
```

Notes: prod uses `ddl-auto=update` (Hibernate creates/updates tables on first boot),
H2 console is disabled, and `DevDataInitializer` does NOT run (no seed data — register
the first user via `/index.html`, then set `verify_status='VERIFIED'` in the DB).

## Gotchas learned the hard way (READ THESE)

- `/api/login` returns a **real JWT** plus explicit `userId`, `fullName`, `role` fields.
  Read the id from `data.userId` (helper `eqfUserId()` in `auth.js`). The old
  `token.split('_')[1]` trick is DEAD — never parse the token on the frontend.
- All frontend API calls must go through `apiFetch()` (shared `static/auth.js`, loaded
  before each page's inline script) so the Bearer header is attached; a plain `fetch()`
  to a protected `/api/**` endpoint gets 401 and bounces the user to the login page.
- When EDITING a file, **preserve all existing features**. Do NOT rewrite a whole file
  and silently drop unrelated code. (The AI Tab feature in questions.html was lost once
  this way.) Make targeted edits; after editing, verify nothing else disappeared.
- H2 resets/append behavior: dev data is seeded by `DevDataInitializer` only when the
  subjects table is empty.

## Prompt patterns that work well here

- **Start of session (grounding):** "Read AGENTS.md and the whole project. Summarize the
  current state and what Slice 4 requires. Don't write code yet."
- **Implement a slice:** "Implement Slice 4 per AGENTS.md: <list steps>. Show the solution
  first, then a short explanation. Then run `mvn spring-boot:run` and fix errors."
- **Fix without regressions:** "Fix only <X> in <file>. Keep every other feature in that
  file intact. List what you changed."
- **After finishing:** "Update the CURRENT STATUS section in AGENTS.md and commit briefly."

The human prefers: the full solution first, then a short explanation of each file.
