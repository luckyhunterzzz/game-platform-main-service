ALTER TABLE event_blocks
    ADD COLUMN image_bucket_json JSONB,
    ADD COLUMN image_object_key_json JSONB;

UPDATE event_blocks
SET image_bucket_json = CASE
        WHEN image_bucket IS NULL OR btrim(image_bucket) = '' THEN NULL
        ELSE jsonb_build_object('ru', image_bucket, 'en', image_bucket)
    END,
    image_object_key_json = CASE
        WHEN image_object_key IS NULL OR btrim(image_object_key) = '' THEN NULL
        ELSE jsonb_build_object('ru', image_object_key, 'en', image_object_key)
    END;

ALTER TABLE event_blocks
    DROP COLUMN image_bucket,
    DROP COLUMN image_object_key;
