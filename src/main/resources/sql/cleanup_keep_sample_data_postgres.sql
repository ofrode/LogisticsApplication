BEGIN;

-- Update these IDs before running the script.
-- The script will automatically preserve linked records required by kept shipments/vehicles.
CREATE TEMP TABLE keep_users (id BIGINT PRIMARY KEY) ON COMMIT DROP;
CREATE TEMP TABLE keep_vehicles (id BIGINT PRIMARY KEY) ON COMMIT DROP;
CREATE TEMP TABLE keep_shipments (id BIGINT PRIMARY KEY) ON COMMIT DROP;

INSERT INTO keep_users (id)
VALUES (5), (6)
ON CONFLICT DO NOTHING;

INSERT INTO keep_vehicles (id)
VALUES (2)
ON CONFLICT DO NOTHING;

INSERT INTO keep_shipments (id)
VALUES (1)
ON CONFLICT DO NOTHING;

-- Keep vehicles attached to shipments we want to preserve.
INSERT INTO keep_vehicles (id)
SELECT DISTINCT sv.vehicle_id
FROM shipment_vehicle sv
JOIN keep_shipments ks ON ks.id = sv.shipment_id
ON CONFLICT DO NOTHING;

-- Keep users referenced by preserved shipments and vehicles.
INSERT INTO keep_users (id)
SELECT DISTINCT s.customer_id
FROM shipments s
JOIN keep_shipments ks ON ks.id = s.id
ON CONFLICT DO NOTHING;

INSERT INTO keep_users (id)
SELECT DISTINCT s.manager_id
FROM shipments s
JOIN keep_shipments ks ON ks.id = s.id
ON CONFLICT DO NOTHING;

INSERT INTO keep_users (id)
SELECT DISTINCT v.carrier_id
FROM vehicles v
JOIN keep_vehicles kv ON kv.id = v.id
ON CONFLICT DO NOTHING;

-- Remove links to objects we are not keeping.
DELETE FROM shipment_vehicle sv
WHERE sv.shipment_id NOT IN (SELECT id FROM keep_shipments)
   OR sv.vehicle_id NOT IN (SELECT id FROM keep_vehicles);

-- Remove dependent shipment records first.
DELETE FROM cargoes
WHERE shipment_id NOT IN (SELECT id FROM keep_shipments);

DELETE FROM shipment_schedules
WHERE shipment_id NOT IN (SELECT id FROM keep_shipments);

DELETE FROM shipments
WHERE id NOT IN (SELECT id FROM keep_shipments);

DELETE FROM vehicles
WHERE id NOT IN (SELECT id FROM keep_vehicles);

DELETE FROM app_users
WHERE id NOT IN (SELECT id FROM keep_users);

-- Optional sequence resync for PostgreSQL identity columns.
SELECT setval(
    pg_get_serial_sequence('app_users', 'id'),
    COALESCE((SELECT MAX(id) FROM app_users), 1),
    (SELECT COUNT(*) > 0 FROM app_users)
);

SELECT setval(
    pg_get_serial_sequence('vehicles', 'id'),
    COALESCE((SELECT MAX(id) FROM vehicles), 1),
    (SELECT COUNT(*) > 0 FROM vehicles)
);

SELECT setval(
    pg_get_serial_sequence('shipments', 'id'),
    COALESCE((SELECT MAX(id) FROM shipments), 1),
    (SELECT COUNT(*) > 0 FROM shipments)
);

SELECT setval(
    pg_get_serial_sequence('cargoes', 'id'),
    COALESCE((SELECT MAX(id) FROM cargoes), 1),
    (SELECT COUNT(*) > 0 FROM cargoes)
);

SELECT setval(
    pg_get_serial_sequence('shipment_schedules', 'id'),
    COALESCE((SELECT MAX(id) FROM shipment_schedules), 1),
    (SELECT COUNT(*) > 0 FROM shipment_schedules)
);

COMMIT;
