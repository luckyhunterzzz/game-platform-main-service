CREATE TABLE outbox_events (
   id UUID PRIMARY KEY,

   aggregate_type VARCHAR(100) NOT NULL,
   aggregate_id UUID NOT NULL,

   event_type VARCHAR(100) NOT NULL,

   payload JSONB NOT NULL,

   created_at TIMESTAMP WITH TIME ZONE NOT NULL,

   processed BOOLEAN NOT NULL DEFAULT FALSE,
   processed_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_outbox_unprocessed_events
    ON outbox_events (created_at)
    WHERE processed = FALSE;

COMMENT ON TABLE outbox_events IS 'Table that stores events before sending them to other systems';
COMMENT ON COLUMN outbox_events.id IS 'Unique ID of the event';
COMMENT ON COLUMN outbox_events.aggregate_type IS 'Type of domain object, for example PUBLICATION';
COMMENT ON COLUMN outbox_events.aggregate_id IS 'ID of the domain object';
COMMENT ON COLUMN outbox_events.event_type IS 'Name of the event, for example CONTENT_CREATED';
COMMENT ON COLUMN outbox_events.payload IS 'Event data stored as JSON';
COMMENT ON COLUMN outbox_events.created_at IS 'Time when the event was saved to the outbox';
COMMENT ON COLUMN outbox_events.processed IS 'Shows if the event was already sent';
COMMENT ON COLUMN outbox_events.processed_at IS 'Time when the event was successfully sent';