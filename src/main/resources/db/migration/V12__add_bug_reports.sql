CREATE TABLE bug_reports
(
    id          UUID PRIMARY KEY,
    hero_id     BIGINT                   NOT NULL REFERENCES heroes (id),
    author_id   UUID NULL,
    author_name VARCHAR(255)             NOT NULL,
    description TEXT                     NOT NULL,
    is_open     BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    closed_at   TIMESTAMP WITH TIME ZONE NULL,
    closed_by   VARCHAR(100) NULL
);

CREATE UNIQUE INDEX idx_open_bug_report_on_hero
    ON bug_reports (hero_id)
    WHERE is_open = TRUE;
