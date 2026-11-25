-- Active: 1795539734427@@127.0.0.1@5432@monolith@monolith
-- V1: Initial LMS database schema
-- Reproduces the original pgAdmin schema for a clean installation.
-- Do not add DROP statements to Flyway migrations.

CREATE SCHEMA IF NOT EXISTS monolith;

CREATE TABLE monolith.app_role (
    role_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT app_role_pkey PRIMARY KEY (role_id),
    CONSTRAINT name UNIQUE (name)
);

CREATE TABLE monolith."user" (
    user_id BIGINT NOT NULL,
    email VARCHAR(155) NOT NULL,
    password_hash VARCHAR NOT NULL,
    first_name VARCHAR(40) NOT NULL,
    last_name VARCHAR(40) NOT NULL,
    phone VARCHAR(15),
    is_active BOOLEAN,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT user_pkey PRIMARY KEY (user_id)
);

CREATE TABLE monolith.user_role (
    user_role_id BIGINT NOT NULL,
    user_id BIGINT,
    role_id BIGINT,
    CONSTRAINT user_role_pkey PRIMARY KEY (user_role_id),
    CONSTRAINT uq_user_id UNIQUE (user_id),
    CONSTRAINT uq_role_id UNIQUE (role_id)
);

CREATE TABLE monolith.category (
    category_id BIGINT,
    name VARCHAR,
    description VARCHAR,
    CONSTRAINT category_pkey PRIMARY KEY (category_id)
);

CREATE TABLE monolith.course (
    course_id BIGINT NOT NULL,
    trainer_id BIGINT NOT NULL,
    category_id BIGINT,
    name VARCHAR(155) NOT NULL,
    description VARCHAR,
    status VARCHAR,
    is_active BOOLEAN,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT course_pkey PRIMARY KEY (course_id)
);

CREATE TABLE monolith.course_module (
    module_id BIGINT NOT NULL,
    course_id BIGINT,
    title VARCHAR(50),
    description VARCHAR,
    "position" BIGINT,
    is_active BOOLEAN,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT course_module_pkey PRIMARY KEY (module_id)
);

CREATE TABLE monolith.lesson (
    lesson_id BIGINT NOT NULL,
    module_id BIGINT,
    title VARCHAR,
    content VARCHAR,
    lesson_type VARCHAR,
    "position" BIGINT,
    is_required BOOLEAN,
    is_active BOOLEAN,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT lesson_pkey PRIMARY KEY (lesson_id)
);

CREATE TABLE monolith.assessment (
    assessment_id BIGINT NOT NULL,
    course_id BIGINT,
    title VARCHAR,
    passing_score BIGINT,
    max_attempts BIGINT,
    CONSTRAINT assessment_pkey PRIMARY KEY (assessment_id)
);

CREATE TABLE monolith.attempt (
    attempt_id BIGINT NOT NULL,
    assessment_id BIGINT,
    user_id BIGINT,
    score BIGINT,
    status VARCHAR,
    submitted_at TIMESTAMPTZ,
    CONSTRAINT attempt_pkey PRIMARY KEY (attempt_id)
);

CREATE TABLE monolith.enrollment (
    enrollment_id BIGINT NOT NULL,
    user_id BIGINT,
    course_id BIGINT,
    status VARCHAR,
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    completed_at TIMESTAMPTZ,
    CONSTRAINT enrollment_pkey PRIMARY KEY (enrollment_id),
    CONSTRAINT uq_user_course_id UNIQUE (user_id, course_id)
);

COMMENT ON TABLE monolith.enrollment IS 'UNIQUE learner/course???';

CREATE TABLE monolith.review (
    review_id BIGINT,
    enrollment_id BIGINT,
    rating BIGINT,
    comment VARCHAR,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT review_pkey PRIMARY KEY (review_id),
    CONSTRAINT uq_enrollment_id UNIQUE (enrollment_id)
);

CREATE TABLE monolith.notification (
    notification_id BIGINT,
    recipient_id BIGINT,
    title VARCHAR,
    message VARCHAR,
    notification_type VARCHAR,
    is_read BOOLEAN,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    read_at TIMESTAMPTZ,
    CONSTRAINT notification_pkey PRIMARY KEY (notification_id)
);

CREATE TABLE monolith.lesson_progress (
    progress_id BIGINT,
    enrollment_id BIGINT,
    lesson_id BIGINT,
    status VARCHAR,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    CONSTRAINT lesson_progress_pkey PRIMARY KEY (progress_id)
);

CREATE TABLE monolith.lesson_resource (
    resource_id BIGINT,
    lesson_id BIGINT,
    title VARCHAR,
    resource_type VARCHAR,
    resource_url VARCHAR,
    "position" BIGINT,
    CONSTRAINT lesson_resource_pkey PRIMARY KEY (resource_id)
);

CREATE TABLE monolith.question (
    question_id BIGINT,
    assessment_id BIGINT,
    prompt VARCHAR,
    points BIGINT,
    "position" BIGINT,
    CONSTRAINT question_pkey PRIMARY KEY (question_id)
);

CREATE TABLE monolith.question_option (
    option_id BIGINT,
    question_id BIGINT,
    option_text VARCHAR,
    is_correct BOOLEAN,
    "position" BIGINT,
    CONSTRAINT question_option_pkey PRIMARY KEY (option_id)
);

CREATE TABLE monolith.attempt_answer (
    answer_id BIGINT,
    attempt_id BIGINT,
    question_id BIGINT,
    selected_option_id BIGINT,
    CONSTRAINT attempt_answer_pkey PRIMARY KEY (answer_id)
);

-- Foreign keys

ALTER TABLE monolith.user_role
    ADD CONSTRAINT fk_role_id
    FOREIGN KEY (role_id) REFERENCES monolith.app_role(role_id);

ALTER TABLE monolith.user_role
    ADD CONSTRAINT fk_user_id
    FOREIGN KEY (user_id) REFERENCES monolith."user"(user_id);

ALTER TABLE monolith.course
    ADD CONSTRAINT fk_trainer_id
    FOREIGN KEY (trainer_id) REFERENCES monolith."user"(user_id);

ALTER TABLE monolith.course
    ADD CONSTRAINT fk_category_id
    FOREIGN KEY (category_id) REFERENCES monolith.category(category_id)
    NOT VALID;

ALTER TABLE monolith.course_module
    ADD CONSTRAINT fk_course_id
    FOREIGN KEY (course_id) REFERENCES monolith.course(course_id);

ALTER TABLE monolith.lesson
    ADD CONSTRAINT fk_module_id
    FOREIGN KEY (module_id) REFERENCES monolith.course_module(module_id);

ALTER TABLE monolith.assessment
    ADD CONSTRAINT fk_course_id
    FOREIGN KEY (course_id) REFERENCES monolith.course(course_id);

ALTER TABLE monolith.attempt
    ADD CONSTRAINT fk_assessment_id
    FOREIGN KEY (assessment_id) REFERENCES monolith.assessment(assessment_id);

ALTER TABLE monolith.attempt
    ADD CONSTRAINT fk_user_id
    FOREIGN KEY (user_id) REFERENCES monolith."user"(user_id);

ALTER TABLE monolith.enrollment
    ADD CONSTRAINT fk_course_id
    FOREIGN KEY (course_id) REFERENCES monolith.course(course_id);

ALTER TABLE monolith.enrollment
    ADD CONSTRAINT fk_user_id
    FOREIGN KEY (user_id) REFERENCES monolith."user"(user_id);

ALTER TABLE monolith.review
    ADD CONSTRAINT fk_enrollment_id
    FOREIGN KEY (enrollment_id) REFERENCES monolith.enrollment(enrollment_id);

ALTER TABLE monolith.notification
    ADD CONSTRAINT fk_user_id
    FOREIGN KEY (recipient_id) REFERENCES monolith."user"(user_id);

ALTER TABLE monolith.lesson_progress
    ADD CONSTRAINT fk_enrollment_id
    FOREIGN KEY (enrollment_id) REFERENCES monolith.enrollment(enrollment_id)
    NOT VALID;

ALTER TABLE monolith.lesson_progress
    ADD CONSTRAINT fk_lesson_id
    FOREIGN KEY (lesson_id) REFERENCES monolith.lesson(lesson_id)
    NOT VALID;

ALTER TABLE monolith.lesson_resource
    ADD CONSTRAINT fk_lesson_id
    FOREIGN KEY (lesson_id) REFERENCES monolith.lesson(lesson_id)
    NOT VALID;

ALTER TABLE monolith.question
    ADD CONSTRAINT fk_assessment_id
    FOREIGN KEY (assessment_id) REFERENCES monolith.assessment(assessment_id)
    NOT VALID;

ALTER TABLE monolith.question_option
    ADD CONSTRAINT fk_question_id
    FOREIGN KEY (question_id) REFERENCES monolith.question(question_id)
    NOT VALID;

ALTER TABLE monolith.attempt_answer
    ADD CONSTRAINT fk_attempt_id
    FOREIGN KEY (attempt_id) REFERENCES monolith.attempt(attempt_id)
    NOT VALID;

ALTER TABLE monolith.attempt_answer
    ADD CONSTRAINT fk_question_id
    FOREIGN KEY (question_id) REFERENCES monolith.question(question_id)
    NOT VALID;

ALTER TABLE monolith.attempt_answer
    ADD CONSTRAINT fk_option_id
    FOREIGN KEY (selected_option_id)
    REFERENCES monolith.question_option(option_id)
    NOT VALID;
