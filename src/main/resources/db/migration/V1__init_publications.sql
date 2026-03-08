CREATE TABLE publications (
    id UUID PRIMARY KEY,
    version INTEGER NOT NULL DEFAULT 0,

    type VARCHAR(50) NOT NULL,
    title VARCHAR(500) NOT NULL,
    content TEXT,

    image_bucket VARCHAR(255),
    image_object_key VARCHAR(1024),

    status VARCHAR(50) NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,

    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    pinned_at TIMESTAMP WITH TIME ZONE,

    created_by UUID,
    updated_by UUID,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_publications_public_feed
    ON publications (is_pinned DESC, pinned_at DESC, published_at DESC)
    WHERE status = 'PUBLISHED';

CREATE INDEX idx_publications_status_published_at
    ON publications (status, published_at);

COMMENT ON TABLE publications IS 'Storage for news, schedules, and guides';
COMMENT ON COLUMN publications.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN publications.version IS 'Version number for optimistic locking';
COMMENT ON COLUMN publications.type IS 'Type of content: NEWS, SCHEDULE, GUIDE, etc.';
COMMENT ON COLUMN publications.title IS 'Main title of the publication';
COMMENT ON COLUMN publications.content IS 'Main text body of the publication';
COMMENT ON COLUMN publications.image_bucket IS 'MinIO bucket name for the attached image';
COMMENT ON COLUMN publications.image_object_key IS 'Path to the image file in MinIO';
COMMENT ON COLUMN publications.status IS 'Current state: DRAFT, SCHEDULED, PUBLISHED, or ARCHIVED';
COMMENT ON COLUMN publications.published_at IS 'Date and time when the post becomes visible to users';
COMMENT ON COLUMN publications.is_pinned IS 'Flag to show this post at the top of the list';
COMMENT ON COLUMN publications.pinned_at IS 'Timestamp when the post was pinned for sorting';
COMMENT ON COLUMN publications.created_by IS 'ID of the admin who created this record';
COMMENT ON COLUMN publications.updated_by IS 'ID of the admin who last changed this record';
COMMENT ON COLUMN publications.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN publications.updated_at IS 'Timestamp of the last update';