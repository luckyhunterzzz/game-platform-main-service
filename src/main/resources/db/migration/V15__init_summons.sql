CREATE TABLE summons
(
    id                          UUID PRIMARY KEY,
    version                     INTEGER                  NOT NULL DEFAULT 0,
    slug                        VARCHAR(150)             NOT NULL UNIQUE,
    name_json                   JSONB                    NOT NULL,
    description_json            JSONB,
    image_bucket                VARCHAR(255),
    image_object_key            VARCHAR(1024),
    background_image_bucket     VARCHAR(255),
    background_image_object_key VARCHAR(1024),
    status                      VARCHAR(30)              NOT NULL,
    created_by                  UUID,
    updated_by                  UUID,
    created_at                  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at                  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE summon_variants (
    id           UUID PRIMARY KEY,
    summon_id    UUID                     NOT NULL REFERENCES summons (id) ON DELETE CASCADE,
    version      INTEGER                  NOT NULL DEFAULT 0,
    variant_type VARCHAR(30)              NOT NULL,
    starts_at    TIMESTAMP WITH TIME ZONE,
    ends_at      TIMESTAMP WITH TIME ZONE,
    status       VARCHAR(30)              NOT NULL,
    created_by   UUID,
    updated_by   UUID,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_summon_variants_period CHECK (
        (variant_type = 'DEFAULT' AND starts_at IS NULL AND ends_at IS NULL)
        OR
        (variant_type = 'SCHEDULED' AND starts_at IS NOT NULL AND ends_at IS NOT NULL AND ends_at > starts_at)
    )
);

CREATE TABLE summon_probability_definitions
(
    id               UUID PRIMARY KEY,
    name_json        JSONB NOT NULL,
    description_json JSONB
);

CREATE TABLE summon_probabilities
(
    id            UUID PRIMARY KEY,
    variant_id    UUID          NOT NULL REFERENCES summon_variants (id) ON DELETE CASCADE,
    definition_id UUID          NOT NULL REFERENCES summon_probability_definitions (id),
    kind          VARCHAR(40)   NOT NULL,
    value_percent NUMERIC(7, 4) NOT NULL,
    position      INTEGER       NOT NULL,
    CONSTRAINT chk_summon_probabilities_value CHECK (value_percent >= 0 AND value_percent <= 100)
);

CREATE TABLE summon_heroes
(
    probability_id UUID   NOT NULL REFERENCES summon_probabilities (id) ON DELETE CASCADE,
    hero_id         BIGINT NOT NULL REFERENCES heroes (id),
    PRIMARY KEY (probability_id, hero_id)
);

CREATE TABLE summon_bonus_rules
(
    id                              UUID PRIMARY KEY,
    variant_id                      UUID        NOT NULL REFERENCES summon_variants (id) ON DELETE CASCADE,
    kind                            VARCHAR(40) NOT NULL,
    user_selectable                 BOOLEAN     NOT NULL DEFAULT FALSE,
    trigger_condition_type          VARCHAR(40),
    trigger_rarity_id               BIGINT REFERENCES rarities (id),
    trigger_probability_id          UUID REFERENCES summon_probabilities (id),
    continuation_condition_type     VARCHAR(40),
    continuation_rarity_id          BIGINT REFERENCES rarities (id),
    continuation_probability_id     UUID REFERENCES summon_probabilities (id)
);

CREATE TABLE summon_bonus_steps
(
    id                        UUID PRIMARY KEY,
    bonus_rule_id             UUID          NOT NULL REFERENCES summon_bonus_rules (id) ON DELETE CASCADE,
    position                  INTEGER       NOT NULL,
    activation_chance_percent NUMERIC(7, 4) NOT NULL,
    reward_mode               VARCHAR(40)   NOT NULL,
    reward_probability_id     UUID REFERENCES summon_probabilities (id),
    CONSTRAINT chk_summon_bonus_steps_chance CHECK (
        activation_chance_percent >= 0 AND activation_chance_percent <= 100
    )
);
