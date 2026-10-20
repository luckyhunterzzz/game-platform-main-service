ALTER TABLE summon_probabilities
    ADD COLUMN user_selection_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN default_selected_hero_count INTEGER;

ALTER TABLE summon_probabilities
    ADD CONSTRAINT chk_summon_probabilities_user_selection CHECK (
        (user_selection_enabled = FALSE AND default_selected_hero_count IS NULL)
        OR
        (user_selection_enabled = TRUE AND default_selected_hero_count BETWEEN 1 AND 10)
    );

UPDATE summon_probabilities
SET kind = 'BONUS'
WHERE kind IN ('INDEPENDENT_BONUS', 'CONDITIONAL_BONUS');
