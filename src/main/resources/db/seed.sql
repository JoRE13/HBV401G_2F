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

-- Demo scenario for a possible presentation:
-- FI001 is intentionally configured with low capacity, and some seats are already reserved.
-- This lets us demonstrate group-size filtering and "flight fills up" behavior.
UPDATE flights
SET capacity = 5
WHERE flight_number = 'FI001';

INSERT INTO passengers (email, full_name, phone, nationality, date_of_birth) VALUES
    ('demo.passenger1@teamf.is', 'Demo Passenger One', '+354-555-0101', 'Icelandic', '1990-05-10'),
    ('demo.passenger2@teamf.is', 'Demo Passenger Two', '+354-555-0102', 'Icelandic', '1992-08-21')
ON CONFLICT (email) DO UPDATE SET
    full_name = EXCLUDED.full_name,
    phone = EXCLUDED.phone,
    nationality = EXCLUDED.nationality,
    date_of_birth = EXCLUDED.date_of_birth;

INSERT INTO reservations (reservation_code, created_at, status, total_price) VALUES
    ('DEMO-LOW-SEATS', '2027-06-01 10:00:00+00', 'CONFIRMED', 258.00)
ON CONFLICT (reservation_code) DO UPDATE SET
    created_at = EXCLUDED.created_at,
    status = EXCLUDED.status,
    total_price = EXCLUDED.total_price;

INSERT INTO reservation_flights (reservation_code, flight_number) VALUES
    ('DEMO-LOW-SEATS', 'FI001')
ON CONFLICT (reservation_code, flight_number) DO NOTHING;

INSERT INTO reservation_items (item_id, reservation_code, passenger_email, price_paid) VALUES
    ('ITEM-DEMO-1', 'DEMO-LOW-SEATS', 'demo.passenger1@teamf.is', 129.00),
    ('ITEM-DEMO-2', 'DEMO-LOW-SEATS', 'demo.passenger2@teamf.is', 129.00)
ON CONFLICT (item_id) DO UPDATE SET
    reservation_code = EXCLUDED.reservation_code,
    passenger_email = EXCLUDED.passenger_email,
    price_paid = EXCLUDED.price_paid;

INSERT INTO reservation_item_flights (item_id, flight_number, seat_id) VALUES
    ('ITEM-DEMO-1', 'FI001', 'S1'),
    ('ITEM-DEMO-2', 'FI001', 'S2')
ON CONFLICT (item_id, flight_number) DO UPDATE SET
    seat_id = EXCLUDED.seat_id;
