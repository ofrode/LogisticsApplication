ALTER TABLE app_users
    ADD COLUMN IF NOT EXISTS first_name VARCHAR(255);

ALTER TABLE app_users
    ADD COLUMN IF NOT EXISTS last_name VARCHAR(255);

UPDATE app_users
SET
    first_name = COALESCE(first_name, split_part(full_name, ' ', 1)),
    last_name = COALESCE(
            last_name,
            NULLIF(trim(substr(full_name, length(split_part(full_name, ' ', 1)) + 1)), '')
    )
WHERE full_name IS NOT NULL;

UPDATE app_users
SET last_name = COALESCE(last_name, first_name, 'Unknown')
WHERE last_name IS NULL;

UPDATE app_users
SET first_name = COALESCE(first_name, 'Unknown')
WHERE first_name IS NULL;

ALTER TABLE app_users
    ALTER COLUMN first_name SET NOT NULL;

ALTER TABLE app_users
    ALTER COLUMN last_name SET NOT NULL;

ALTER TABLE app_users
    DROP COLUMN IF EXISTS full_name;
