-- Создаем таблицу user_ratings
CREATE TABLE user_ratings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    average_score DECIMAL(5,2) DEFAULT 0.00,
    total_attempts INTEGER DEFAULT 0,
    completed_quizzes INTEGER DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT user_ratings_user_id_fk FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Создаем записи рейтингов для всех существующих пользователей
INSERT INTO user_ratings (user_id, average_score, total_attempts, completed_quizzes, updated_at)
SELECT 
    u.id,
    COALESCE(
        (SELECT CAST(AVG(CAST(qa.score AS DECIMAL) / CAST(qa.total_questions AS DECIMAL) * 100) AS DECIMAL(5,2))
        FROM quiz_attempts qa
        WHERE qa.user_id = u.id AND qa.is_completed = true),
        0.00
    ) as average_score,
    COALESCE(
        (SELECT COUNT(*)
        FROM quiz_attempts qa
        WHERE qa.user_id = u.id AND qa.is_completed = true),
        0
    ) as total_attempts,
    COALESCE(
        (SELECT COUNT(DISTINCT quiz_id)
        FROM quiz_attempts qa
        WHERE qa.user_id = u.id AND qa.is_completed = true),
        0
    ) as completed_quizzes,
    CURRENT_TIMESTAMP as updated_at
FROM users u
WHERE NOT EXISTS (
    SELECT 1 FROM user_ratings ur WHERE ur.user_id = u.id
); 