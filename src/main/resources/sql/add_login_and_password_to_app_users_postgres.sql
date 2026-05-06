BEGIN;

ALTER TABLE app_users
ADD COLUMN IF NOT EXISTS login VARCHAR(100),
ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

UPDATE app_users
SET login = LOWER(TRIM(email))
WHERE login IS NULL;

UPDATE app_users
SET password_hash = '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi6M6r9Ga6IpiObOqYkbKx2GyY8K4Ke'
WHERE password_hash IS NULL;

ALTER TABLE app_users
ALTER COLUMN login SET NOT NULL,
ALTER COLUMN password_hash SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_app_users_login'
    ) THEN
        ALTER TABLE app_users
        ADD CONSTRAINT uk_app_users_login UNIQUE (login);
    END IF;
END $$;

COMMIT;
