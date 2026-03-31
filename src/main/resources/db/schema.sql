CREATE TABLE IF NOT EXISTS airports (
    airport_code VARCHAR(8) PRIMARY KEY,
    name         VARCHAR(120) NOT NULL,
    city         VARCHAR(120) NOT NULL,
    country      VARCHAR(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS flights (
    flight_number          VARCHAR(20) PRIMARY KEY,
    departure_at           TIMESTAMPTZ NOT NULL,
    arrival_at             TIMESTAMPTZ NOT NULL,
    duration_minutes       INTEGER NOT NULL CHECK (duration_minutes >= 0),
    base_price             NUMERIC(10,2) NOT NULL CHECK (base_price >= 0),
    status                 VARCHAR(20) NOT NULL CHECK (status IN ('SCHEDULED','DELAYED','CANCELLED','DEPARTED','ARRIVED')),
    capacity               INTEGER NOT NULL CHECK (capacity > 0),
    departure_airport_code VARCHAR(8) NOT NULL REFERENCES airports(airport_code),
    arrival_airport_code   VARCHAR(8) NOT NULL REFERENCES airports(airport_code),
    airplane_type          VARCHAR(60) NOT NULL,
    CHECK (arrival_at > departure_at)
);

CREATE TABLE IF NOT EXISTS passengers (
    email         VARCHAR(255) PRIMARY KEY,
    full_name     VARCHAR(150) NOT NULL,
    phone         VARCHAR(50) NOT NULL,
    nationality   VARCHAR(80) NOT NULL,
    date_of_birth DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS reservations (
    reservation_code VARCHAR(40) PRIMARY KEY,
    created_at       TIMESTAMPTZ NOT NULL,
    status           VARCHAR(20) NOT NULL CHECK (status IN ('PENDING','CONFIRMED','CANCELLED')),
    total_price      NUMERIC(12,2) NOT NULL CHECK (total_price >= 0)
);

CREATE TABLE IF NOT EXISTS reservation_items (
    item_id           VARCHAR(40) PRIMARY KEY,
    reservation_code  VARCHAR(40) NOT NULL REFERENCES reservations(reservation_code) ON DELETE CASCADE,
    passenger_email   VARCHAR(255) NOT NULL REFERENCES passengers(email),
    price_paid        NUMERIC(12,2) NOT NULL CHECK (price_paid >= 0)
);

CREATE TABLE IF NOT EXISTS reservation_item_flights (
    item_id        VARCHAR(40) NOT NULL REFERENCES reservation_items(item_id) ON DELETE CASCADE,
    flight_number  VARCHAR(20) NOT NULL REFERENCES flights(flight_number) ON DELETE CASCADE,
    seat_id        VARCHAR(20) NOT NULL,
    PRIMARY KEY (item_id, flight_number)
);

CREATE TABLE IF NOT EXISTS reservation_flights (
    reservation_code  VARCHAR(40) NOT NULL REFERENCES reservations(reservation_code) ON DELETE CASCADE,
    flight_number     VARCHAR(20) NOT NULL REFERENCES flights(flight_number) ON DELETE CASCADE,
    PRIMARY KEY (reservation_code, flight_number)
);

CREATE INDEX IF NOT EXISTS idx_flights_departure_airport
    ON flights(departure_airport_code);

CREATE INDEX IF NOT EXISTS idx_flights_arrival_airport
    ON flights(arrival_airport_code);

CREATE INDEX IF NOT EXISTS idx_flights_route_date
    ON flights(
        departure_airport_code,
        arrival_airport_code,
        ((departure_at AT TIME ZONE 'UTC')::date)
    );

CREATE INDEX IF NOT EXISTS idx_flights_departure_date
    ON flights(
        departure_airport_code,
        ((departure_at AT TIME ZONE 'UTC')::date)
    );

CREATE INDEX IF NOT EXISTS idx_reservation_flights_flight_number
    ON reservation_flights(flight_number);

CREATE UNIQUE INDEX IF NOT EXISTS uq_reservation_item_flights_flight_seat
    ON reservation_item_flights(flight_number, seat_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_reservation_items_passenger_per_reservation
    ON reservation_items(reservation_code, passenger_email);
