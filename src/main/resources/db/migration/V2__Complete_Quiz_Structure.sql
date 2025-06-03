-- Create quizzes table
CREATE TABLE quizzes (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(255) NOT NULL,
    result_type VARCHAR(255),
    difficulty VARCHAR(255) NOT NULL DEFAULT 'средний',
    time_duration INTEGER,
    creator_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create quiz_tags table
CREATE TABLE quiz_tags (
    quiz_id BIGINT REFERENCES quizzes(id) ON DELETE CASCADE,
    tag VARCHAR(255) NOT NULL
);

-- Create questions table
CREATE TABLE questions (
    id BIGSERIAL PRIMARY KEY,
    question TEXT NOT NULL,
    image TEXT,
    correct_index INTEGER,
    quiz_id BIGINT REFERENCES quizzes(id) ON DELETE CASCADE
);

-- Create options table
CREATE TABLE options (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    question_id BIGINT REFERENCES questions(id) ON DELETE CASCADE
);

-- Create quiz_results table
CREATE TABLE quiz_results (
    id BIGSERIAL PRIMARY KEY,
    description TEXT,
    image TEXT,
    min_score INTEGER,
    max_score INTEGER,
    target_answer INTEGER,
    quiz_id BIGINT REFERENCES quizzes(id) ON DELETE CASCADE
); 