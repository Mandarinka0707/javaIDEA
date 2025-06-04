CREATE TABLE feed_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    quiz_id BIGINT NOT NULL REFERENCES quizzes(id),
    quiz_title VARCHAR(255) NOT NULL,
    quiz_type VARCHAR(50) NOT NULL,
    completed_at TIMESTAMP NOT NULL,
    score INTEGER,
    total_questions INTEGER,
    time_spent INTEGER,
    position INTEGER,
    character VARCHAR(255),
    description TEXT,
    image VARCHAR(255)
);

CREATE TABLE feed_item_traits (
    feed_item_id BIGINT NOT NULL REFERENCES feed_items(id),
    trait_name VARCHAR(100) NOT NULL,
    trait_value INTEGER NOT NULL,
    PRIMARY KEY (feed_item_id, trait_name)
); 