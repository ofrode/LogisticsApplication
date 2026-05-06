BEGIN;

TRUNCATE TABLE
    shipment_vehicle,
    cargoes,
    shipment_schedules,
    shipments,
    vehicles,
    app_users,
    shipment_statuses,
    user_roles
RESTART IDENTITY CASCADE;

COMMIT;
