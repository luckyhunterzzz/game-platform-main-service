ALTER TABLE publications
    ADD COLUMN pinned_until TIMESTAMP WITH TIME ZONE,
    ADD COLUMN show_in_news_feed BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_publications_pinned_until
    ON publications (pinned_until)
    WHERE is_pinned = TRUE;

COMMENT ON COLUMN publications.pinned_until IS 'Optional moment until the publication stays pinned';
COMMENT ON COLUMN publications.show_in_news_feed IS 'When true, an alliance post can also appear in the news feed';
