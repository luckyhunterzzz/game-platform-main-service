ALTER TABLE summon_probabilities
    ALTER COLUMN value_percent TYPE NUMERIC(5, 2)
        USING ROUND(value_percent, 2);

ALTER TABLE summon_bonus_steps
    ALTER COLUMN activation_chance_percent TYPE NUMERIC(5, 2)
        USING ROUND(activation_chance_percent, 2);
