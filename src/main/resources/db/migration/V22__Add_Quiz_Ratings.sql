-- Add rating columns to quizzes table
ALTER TABLE quizzes ADD COLUMN average_rating DECIMAL(3,2) DEFAULT 0;
ALTER TABLE quizzes ADD COLUMN rating_count INTEGER DEFAULT 0;

-- Create quiz ratings table
CREATE TABLE quiz_ratings (
    id BIGSERIAL PRIMARY KEY,
    quiz_id BIGINT NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(id),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (quiz_id, user_id)
); 