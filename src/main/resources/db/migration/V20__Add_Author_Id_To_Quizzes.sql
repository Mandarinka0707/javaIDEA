ALTER TABLE quizzes ADD COLUMN author_id BIGINT;
ALTER TABLE quizzes ADD CONSTRAINT fk_quiz_author FOREIGN KEY (author_id) REFERENCES users(id); 