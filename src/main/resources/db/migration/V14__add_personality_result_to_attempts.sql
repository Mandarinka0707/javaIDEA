ALTER TABLE quiz_attempts ADD COLUMN personality_result_id BIGINT;
ALTER TABLE quiz_attempts ADD CONSTRAINT fk_quiz_attempt_personality_result 
    FOREIGN KEY (personality_result_id) REFERENCES quiz_results(id); 