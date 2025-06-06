ALTER TABLE feed_items ADD COLUMN personality_result_id BIGINT;
ALTER TABLE feed_items ADD CONSTRAINT fk_feed_item_personality_result 
    FOREIGN KEY (personality_result_id) REFERENCES quiz_results(id); 