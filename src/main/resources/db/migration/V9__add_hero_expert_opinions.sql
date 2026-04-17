CREATE TABLE hero_expert_opinions (
    id BIGSERIAL PRIMARY KEY,
    hero_id BIGINT NOT NULL REFERENCES heroes(id) ON DELETE CASCADE,
    author_name VARCHAR(120) NOT NULL,
    source_url VARCHAR(2048),
    source_title VARCHAR(255),
    source_type VARCHAR(50),
    content_json JSONB NOT NULL,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    published_at DATE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_hero_expert_opinions_published_at
        CHECK (NOT is_published OR published_at IS NOT NULL)
);

CREATE INDEX idx_hero_expert_opinions_hero_id
    ON hero_expert_opinions (hero_id);

CREATE INDEX idx_hero_expert_opinions_public_sort
    ON hero_expert_opinions (hero_id, is_published, published_at DESC, id DESC);
