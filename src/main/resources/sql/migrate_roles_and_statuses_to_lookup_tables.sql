BEGIN;

CREATE TABLE IF NOT EXISTS user_roles (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS shipment_statuses (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO user_roles (code)
VALUES ('ADMIN'), ('MANAGER'), ('CUSTOMER'), ('CARRIER')
ON CONFLICT (code) DO NOTHING;

INSERT INTO shipment_statuses (code)
VALUES ('CREATED'), ('RECEIVED'), ('IN_TRANSIT'), ('DELIVERED'), ('CANCELLED')
ON CONFLICT (code) DO NOTHING;

ALTER TABLE app_users
ADD COLUMN IF NOT EXISTS role_id BIGINT;

UPDATE app_users u
SET role_id = r.id
FROM user_roles r
WHERE u.role = r.code
  AND (u.role_id IS NULL OR u.role_id <> r.id);

ALTER TABLE app_users
ALTER COLUMN role_id SET NOT NULL;

ALTER TABLE app_users
DROP COLUMN IF EXISTS role;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_app_users_role_id'
    ) THEN
        ALTER TABLE app_users
        ADD CONSTRAINT fk_app_users_role_id
        FOREIGN KEY (role_id) REFERENCES user_roles (id);
    END IF;
END $$;

ALTER TABLE shipments
ADD COLUMN IF NOT EXISTS status_id BIGINT;

UPDATE shipments s
SET status_id = st.id
FROM shipment_statuses st
WHERE s.status = st.code
  AND (s.status_id IS NULL OR s.status_id <> st.id);

ALTER TABLE shipments
ALTER COLUMN status_id SET NOT NULL;

ALTER TABLE shipments
DROP COLUMN IF EXISTS status;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_shipments_status_id'
    ) THEN
        ALTER TABLE shipments
        ADD CONSTRAINT fk_shipments_status_id
        FOREIGN KEY (status_id) REFERENCES shipment_statuses (id);
    END IF;
END $$;

COMMIT;
