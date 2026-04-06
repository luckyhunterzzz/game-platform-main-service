ALTER TABLE heroes ADD COLUMN updated_by_email VARCHAR(255);

UPDATE heroes
SET updated_by_email = updated_by
WHERE updated_by_email IS NULL
  AND updated_by LIKE '%@%';
