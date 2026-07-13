# CLAUDE.md — Exam Query Forge (EQF)

> This file is auto-loaded by Claude Code every session. It is the single source
> of truth for project context, conventions, and the build plan. Keep it updated
> as slices are completed.

## What this project is

Exam Query Forge (EQF) is a web app **for verified teachers** to manage exam
questions collaboratively. Core idea: replace the traditional random-draw ("bốc
thăm") way of building exams with a quality-controlled pipeline.

Flow:
1. Verified teachers contribute questions to a **shared pool** (tagged by subject,
   chapter, and difficulty).
2. For an exam, a department head defines an **exam matrix** (e.g. 50 questions:
   80% recognition + low-application, 20% high-application).
3. The system pulls a **wide candidate pool** per difficulty bucket.
4. Subject teachers **vote** on candidates (+1 use / 0 needs-fix / -1 reject;
   comment required for 0 and -1).
5. The system selects the top-voted questions per bucket until the matrix is filled.
6. The department head finalizes; only they see the final exam.

**Security principle:** teachers vote on a wide candidate pool but never learn
which questions made the final exam. This makes leaks low-value by design.

## Tech stack

- Java 17, Spring Boot 3.3.5, Maven
- Spring Data JPA + Hibernate
- **Dev DB: H2 (file-based)** — zero install. **Prod DB: PostgreSQL** (later).
  Entities are DB-agnostic; switching is a dependency + config change only.
- Spring Security (BCrypt password hashing, role-based access)
- Frontend: static HTML/CSS/JS (may move to Thymeleaf/React later)

## Conventions

- Base package: `com.eqf`
- Layout: `model/` (entities + enums), `repository/`, `service/`, `controller/`,
  `config/`, `exception/`
- Enums stored as **VARCHAR via `@Enumerated(EnumType.STRING)`** (not native PG enums).
- Timestamps via Hibernate `@CreationTimestamp` / `@UpdateTimestamp`.
- Passwords: **never store plaintext.** Always BCrypt. Never log raw passwords.
- `src/main/resources/db/schema.postgres.sql` is the **reference** Postgres schema.
  It is Postgres-specific (TIMESTAMPTZ, IDENTITY, triggers) and must **NOT** be
  auto-run on H2. Keep `spring.sql.init.mode=never`.

## Data model (13 tables — see schema.postgres.sql for full DDL)

People & verification: `subjects`, `users`, `credentials`
Question bank: `chapters`, `questions`, `answer_options`, `ai_analyses`
Exams & voting: `exams`, `exam_matrix`, `exam_candidates`, `votes`
Security & history: `audit_log`, `question_usage_history`

Key design points:
- `votes` reference `exam_candidates` (per-exam context), **not** raw `questions`.
- `ai_analyses` holds AI difficulty **suggestions + reasoning only**; the human-set
  `questions.difficulty` is authoritative. AI never overrides.
- Candidate pulling filters by subject + difficulty + status, excludes questions in
  cooldown (recently used, via `question_usage_history`), pulls ~2.5–3× the required
  count per bucket, prioritizing least-recently-used + AI-match + author diversity.

## Build approach: VERTICAL SLICES

Build one feature end-to-end (entity → repository → service → controller → view)
until it runs, then move on. Do **not** build all entities, then all repos, etc.
Weave security (audit log, subject/role filtering) into each slice's controllers.

Slice order:
1. **Auth & User** — Subject/User/Credential entities, repos, BCrypt, basic security.  ← NEXT
2. **Question bank** — Chapter/Question/AnswerOption, CRUD.
3. **AI difficulty analysis** — suggestion + reasoning, plugged into question creation.
4. **Exams + matrix + candidate pulling.**
5. **Voting + selection + finalize.**

## Current status (update me as you go)

- [x] Project renamed to `com.eqf`, artifact `exam-query-forge`
- [x] schema.sql present (move to `resources/db/schema.postgres.sql`, keep as reference)
- [x] Slices 1–5 done (auth, question bank, AI stub, exams+matrix, voting+finalize).
- [x] Slice 6 — real JWT auth (`com.eqf.security`, Bearer tokens, shared `static/auth.js`).
- [x] Slice 7 — production PostgreSQL: profile `prod` (`application-prod.properties`,
      env-driven), multi-stage `Dockerfile`, DevDataInitializer only on default/dev.
- **AGENTS.md is the up-to-date source of truth for status, gotchas, and deploy steps.**

## How to work with me here

- I prefer **the solution first, then a short explanation** of each file.
- After changes, run `mvn spring-boot:run` (or `mvn compile`) and fix any errors
  before moving on.
- Keep slices small and runnable. Confirm before large refactors.
- Bilingual is fine (Vietnamese / English).
