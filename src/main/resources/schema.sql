
CREATE TABLE subjects (
    id    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name  VARCHAR(100) NOT NULL UNIQUE          -- Toán, Lý, Hóa, ...
);

CREATE TABLE users (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    full_name      VARCHAR(150) NOT NULL,
    email          VARCHAR(255) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    role           VARCHAR(20)  NOT NULL DEFAULT 'TEACHER'
                   CHECK (role IN ('TEACHER','DEPARTMENT_HEAD','ADMIN')),
    subject_id     BIGINT REFERENCES subjects(id),   -- NULL allowed for ADMIN
    verify_status  VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                   CHECK (verify_status IN ('PENDING','VERIFIED','REJECTED')),
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- A teacher uploads one or more degrees/certificates; an admin/dept head reviews.
CREATE TABLE credentials (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id          BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    credential_type  VARCHAR(50) NOT NULL,           -- DEGREE, CERTIFICATE, ...
    doc_url          VARCHAR(500) NOT NULL,          -- uploaded file location
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                     CHECK (status IN ('PENDING','VERIFIED','REJECTED')),
    verified_by      BIGINT REFERENCES users(id),    -- reviewer
    verified_at      TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- =====================================================================
--  2. Question bank
-- =====================================================================

CREATE TABLE chapters (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    subject_id  BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    name        VARCHAR(200) NOT NULL,
    grade       SMALLINT,                            -- 10 / 11 / 12
    UNIQUE (subject_id, name, grade)
);

CREATE TABLE questions (
    id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    subject_id         BIGINT NOT NULL REFERENCES subjects(id),
    chapter_id         BIGINT REFERENCES chapters(id),
    author_id          BIGINT NOT NULL REFERENCES users(id),
    content            TEXT NOT NULL,
    question_type      VARCHAR(20) NOT NULL
                       CHECK (question_type IN ('MULTIPLE_CHOICE','TRUE_FALSE','ESSAY')),
    -- 4 levels: nhận biết / thông hiểu / vận dụng / vận dụng cao
    difficulty         VARCHAR(20) NOT NULL
                       CHECK (difficulty IN ('RECOGNITION','COMPREHENSION','APPLICATION','HIGH_APPLICATION')),
    -- who set the official difficulty (AI only suggests; final decision is human)
    difficulty_source  VARCHAR(20) NOT NULL DEFAULT 'AUTHOR'
                       CHECK (difficulty_source IN ('AUTHOR','AI','DEPT_HEAD')),
    status             VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                       CHECK (status IN ('DRAFT','IN_POOL','ARCHIVED')),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- For MULTIPLE_CHOICE / TRUE_FALSE. Essay questions simply have no options.
CREATE TABLE answer_options (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    question_id  BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    content      TEXT NOT NULL,
    is_correct   BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order   SMALLINT
);

-- AI difficulty suggestion + reasoning. Suggestion only, never overrides
-- questions.difficulty. Re-analysis allowed -> query the latest row.
CREATE TABLE ai_analyses (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    question_id           BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    suggested_difficulty  VARCHAR(20) NOT NULL
                          CHECK (suggested_difficulty IN ('RECOGNITION','COMPREHENSION','APPLICATION','HIGH_APPLICATION')),
    reasoning             TEXT,
    model_name            VARCHAR(100),
    analyzed_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- =====================================================================
--  3. Exams, matrix, candidates & voting
-- =====================================================================

CREATE TABLE exams (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title                 VARCHAR(255) NOT NULL,
    subject_id            BIGINT NOT NULL REFERENCES subjects(id),
    grade                 SMALLINT,
    total_questions       INT NOT NULL,
    status                VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                          CHECK (status IN ('DRAFT','REVIEW','FINALIZED','PUBLISHED')),
    -- how many candidates to pull per required slot (e.g. 2.5x)
    candidate_multiplier  NUMERIC(3,1) NOT NULL DEFAULT 2.5,
    created_by            BIGINT NOT NULL REFERENCES users(id),
    exam_date             DATE,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- The "ma trận đề": one row per difficulty bucket = "this level needs N questions".
-- (Per-chapter quotas can be added later as a future extension.)
CREATE TABLE exam_matrix (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    exam_id         BIGINT NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    difficulty      VARCHAR(20) NOT NULL
                    CHECK (difficulty IN ('RECOGNITION','COMPREHENSION','APPLICATION','HIGH_APPLICATION')),
    required_count  INT NOT NULL CHECK (required_count > 0),
    UNIQUE (exam_id, difficulty)
);

-- Sub-pool of candidate questions for ONE exam. Teachers vote here.
-- status: CANDIDATE (under review) -> SELECTED (made it into the exam) / REJECTED.
CREATE TABLE exam_candidates (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    exam_id      BIGINT NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    question_id  BIGINT NOT NULL REFERENCES questions(id),
    status       VARCHAR(20) NOT NULL DEFAULT 'CANDIDATE'
                 CHECK (status IN ('CANDIDATE','SELECTED','REJECTED')),
    added_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (exam_id, question_id)
);

-- Votes are tied to a CANDIDATE (per-exam context), not to the raw question.
-- value: +1 = Nên dùng, 0 = Cần sửa, -1 = Loại. Comment required for 0 and -1.
CREATE TABLE votes (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    candidate_id  BIGINT NOT NULL REFERENCES exam_candidates(id) ON DELETE CASCADE,
    voter_id      BIGINT NOT NULL REFERENCES users(id),
    value         SMALLINT NOT NULL CHECK (value IN (-1, 0, 1)),
    comment       TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (candidate_id, voter_id),                 -- one vote per teacher per candidate
    CHECK (value = 1 OR comment IS NOT NULL)         -- force feedback when not "Nên dùng"
);


-- =====================================================================
--  4. Security & history
-- =====================================================================

-- Every time someone views a question / candidate, log it. Deterrent + traceability.
CREATE TABLE audit_log (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id      BIGINT REFERENCES users(id),
    action       VARCHAR(50) NOT NULL,               -- VIEW_QUESTION, VIEW_CANDIDATE, ...
    question_id  BIGINT REFERENCES questions(id),
    exam_id      BIGINT REFERENCES exams(id),
    ip_address   VARCHAR(45),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- One row each time a question is used in a real (published) exam.
-- Powers the cooldown filter in the candidate-pulling query.
CREATE TABLE question_usage_history (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    question_id  BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    exam_id      BIGINT NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    used_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (question_id, exam_id)
);


-- =====================================================================
--  5. Indexes (tuned for the hot paths)
-- =====================================================================

-- candidate-pulling query: filter by subject + difficulty + status
CREATE INDEX idx_questions_pool       ON questions (subject_id, difficulty, status);
CREATE INDEX idx_questions_author     ON questions (author_id);
CREATE INDEX idx_chapters_subject     ON chapters (subject_id);

-- cooldown lookups
CREATE INDEX idx_usage_question       ON question_usage_history (question_id, used_at);
CREATE INDEX idx_usage_used_at        ON question_usage_history (used_at);

-- vote tallying per exam
CREATE INDEX idx_votes_candidate      ON votes (candidate_id);
CREATE INDEX idx_candidates_exam      ON exam_candidates (exam_id, status);

-- latest AI analysis per question
CREATE INDEX idx_ai_question          ON ai_analyses (question_id, analyzed_at DESC);


-- =====================================================================
--  6. updated_at auto-touch trigger
-- =====================================================================

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated     BEFORE UPDATE ON users     FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_questions_updated BEFORE UPDATE ON questions FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_exams_updated     BEFORE UPDATE ON exams     FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_votes_updated     BEFORE UPDATE ON votes     FOR EACH ROW EXECUTE FUNCTION set_updated_at();


-- =====================================================================
--  7. Minimal seed (optional — adjust to your school)
-- =====================================================================

-- INSERT INTO subjects (name) VALUES ('Toán'), ('Vật lý'), ('Hóa học'), ('Ngữ văn');
