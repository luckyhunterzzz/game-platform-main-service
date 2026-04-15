ALTER TABLE passive_skills
    ADD COLUMN image_bucket VARCHAR(255),
    ADD COLUMN image_object_key VARCHAR(1024);

ALTER TABLE families
    ADD COLUMN image_bucket VARCHAR(255),
    ADD COLUMN image_object_key VARCHAR(1024);

ALTER TABLE hero_classes
    ADD COLUMN image_bucket VARCHAR(255),
    ADD COLUMN image_object_key VARCHAR(1024);

ALTER TABLE alpha_talents
    ADD COLUMN image_bucket VARCHAR(255),
    ADD COLUMN image_object_key VARCHAR(1024);

ALTER TABLE elements
    ADD COLUMN image_bucket VARCHAR(255),
    ADD COLUMN image_object_key VARCHAR(1024);

ALTER TABLE rarities
    ADD COLUMN image_bucket VARCHAR(255),
    ADD COLUMN image_object_key VARCHAR(1024);
