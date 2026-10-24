ALTER TABLE summon_bonus_steps
    ADD COLUMN user_hero_selection_enabled BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE summon_bonus_step_heroes
(
    bonus_step_id UUID   NOT NULL REFERENCES summon_bonus_steps (id) ON DELETE CASCADE,
    hero_id       BIGINT NOT NULL REFERENCES heroes (id),
    PRIMARY KEY (bonus_step_id, hero_id)
);
