ALTER TABLE summon_probabilities
    ADD COLUMN hero_variant_mode VARCHAR(32) NOT NULL DEFAULT 'EXACT';

ALTER TABLE summon_probabilities
    ADD CONSTRAINT chk_summon_probabilities_hero_variant_mode
        CHECK (hero_variant_mode IN ('EXACT', 'ORIGINAL', 'LATEST_COSTUME'));
