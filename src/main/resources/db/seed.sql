-- Seed data for Team F (Flight Search)
-- Scope: Icelandic airports only, with domestic Iceland flights.
-- Safe to run multiple times.

INSERT INTO airports (airport_code, name, city, country) VALUES
    ('KEF', 'Keflavik International Airport', 'Reykjanesbaer', 'Iceland'),
    ('RKV', 'Reykjavik Airport', 'Reykjavik', 'Iceland'),
    ('AEY', 'Akureyri Airport', 'Akureyri', 'Iceland'),
    ('IFJ', 'Isafjordur Airport', 'Isafjordur', 'Iceland'),
    ('EGS', 'Egilsstadir Airport', 'Egilsstadir', 'Iceland'),
    ('HZK', 'Husavik Airport', 'Husavik', 'Iceland')
ON CONFLICT (airport_code) DO UPDATE SET
    name = EXCLUDED.name,
    city = EXCLUDED.city,
    country = EXCLUDED.country;

INSERT INTO flights (
    flight_number,
    departure_at,
    arrival_at,
    duration_minutes,
    base_price,
    status,
    capacity,
    departure_airport_code,
    arrival_airport_code,
    airplane_type
) VALUES
    ('FI001', '2027-06-10 07:30:00+00', '2027-06-10 08:20:00+00', 50, 129.00, 'SCHEDULED', 72, 'RKV', 'AEY', 'DHC-8'),
    ('FI002', '2027-06-10 09:10:00+00', '2027-06-10 10:05:00+00', 55, 139.00, 'SCHEDULED', 72, 'AEY', 'RKV', 'DHC-8'),
    ('FI003', '2027-06-10 11:00:00+00', '2027-06-10 11:45:00+00', 45, 119.00, 'SCHEDULED', 50, 'RKV', 'IFJ', 'DHC-8'),
    ('FI004', '2027-06-10 12:40:00+00', '2027-06-10 13:25:00+00', 45, 119.00, 'SCHEDULED', 50, 'IFJ', 'RKV', 'DHC-8'),
    ('FI005', '2027-06-11 08:15:00+00', '2027-06-11 09:10:00+00', 55, 149.00, 'SCHEDULED', 76, 'RKV', 'EGS', 'Q400'),
    ('FI006', '2027-06-11 10:00:00+00', '2027-06-11 10:55:00+00', 55, 149.00, 'SCHEDULED', 76, 'EGS', 'RKV', 'Q400'),
    ('FI007', '2027-06-11 13:20:00+00', '2027-06-11 14:05:00+00', 45, 109.00, 'SCHEDULED', 45, 'RKV', 'HZK', 'DHC-6'),
    ('FI008', '2027-06-11 14:50:00+00', '2027-06-11 15:35:00+00', 45, 109.00, 'SCHEDULED', 45, 'HZK', 'RKV', 'DHC-6'),
    ('FI009', '2027-06-12 07:45:00+00', '2027-06-12 08:35:00+00', 50, 125.00, 'SCHEDULED', 68, 'KEF', 'AEY', 'E190'),
    ('FI010', '2027-06-12 16:20:00+00', '2027-06-12 17:10:00+00', 50, 125.00, 'SCHEDULED', 68, 'AEY', 'KEF', 'E190'),
    ('FI011', '2027-06-12 09:30:00+00', '2027-06-12 10:20:00+00', 50, 135.00, 'SCHEDULED', 68, 'KEF', 'EGS', 'E190'),
    ('FI012', '2027-06-12 18:00:00+00', '2027-06-12 18:50:00+00', 50, 135.00, 'SCHEDULED', 68, 'EGS', 'KEF', 'E190')
ON CONFLICT (flight_number) DO UPDATE SET
    departure_at = EXCLUDED.departure_at,
    arrival_at = EXCLUDED.arrival_at,
    duration_minutes = EXCLUDED.duration_minutes,
    base_price = EXCLUDED.base_price,
    status = EXCLUDED.status,
    capacity = EXCLUDED.capacity,
    departure_airport_code = EXCLUDED.departure_airport_code,
    arrival_airport_code = EXCLUDED.arrival_airport_code,
    airplane_type = EXCLUDED.airplane_type;
