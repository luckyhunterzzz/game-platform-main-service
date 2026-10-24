CREATE TABLE summon_bonus_step_probabilities
(
    bonus_step_id    UUID          NOT NULL REFERENCES summon_bonus_steps (id) ON DELETE CASCADE,
    probability_id   UUID          NOT NULL REFERENCES summon_probabilities (id) ON DELETE CASCADE,
    value_percent    NUMERIC(5, 2) NOT NULL,
    position         INTEGER       NOT NULL,
    PRIMARY KEY (bonus_step_id, probability_id),
    CONSTRAINT chk_summon_bonus_step_probabilities_value CHECK (
        value_percent > 0 AND value_percent <= 100
    )
);

INSERT INTO summon_bonus_step_probabilities (bonus_step_id, probability_id, value_percent, position)
SELECT id, reward_probability_id, 100.00, 1
FROM summon_bonus_steps
WHERE reward_probability_id IS NOT NULL;

UPDATE summon_bonus_steps
SET reward_mode = 'CUSTOM_DISTRIBUTION'
WHERE reward_mode = 'SPECIFIC_PROBABILITY';

ALTER TABLE summon_bonus_steps
    DROP COLUMN reward_probability_id;
