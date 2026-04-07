TRUNCATE TABLE rarity_evolution_multipliers RESTART IDENTITY;

TRUNCATE TABLE hero_class_emblem_bonus_profiles RESTART IDENTITY;

ALTER TABLE hero_class_emblem_bonus_profiles
    RENAME COLUMN attack_bonus TO attack_flat_bonus;

ALTER TABLE hero_class_emblem_bonus_profiles
    RENAME COLUMN armor_bonus TO armor_flat_bonus;

ALTER TABLE hero_class_emblem_bonus_profiles
    RENAME COLUMN hp_bonus TO hp_flat_bonus;

ALTER TABLE hero_class_emblem_bonus_profiles
    ADD COLUMN attack_percent_bonus NUMERIC(6, 4) NOT NULL DEFAULT 0.0000,
    ADD COLUMN armor_percent_bonus NUMERIC(6, 4) NOT NULL DEFAULT 0.0000,
    ADD COLUMN hp_percent_bonus NUMERIC(6, 4) NOT NULL DEFAULT 0.0000,
    ADD COLUMN master_attack_bonus INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN master_armor_bonus INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN master_hp_bonus INTEGER NOT NULL DEFAULT 0;

ALTER TABLE heroes
    ADD COLUMN costume_index INTEGER;

CREATE UNIQUE INDEX uq_heroes_base_hero_costume_index
    ON heroes (base_hero_id, costume_index)
    WHERE costume_index IS NOT NULL;
