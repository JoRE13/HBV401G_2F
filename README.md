# Team F: Flight Search Component (HBV401G)

This repository contains Team F's flight search and booking component for the travel portal project.

## What this component does

- Searches flights involving Icelandic airports
- Supports reservation flow:
  - create reservation from itinerary
  - add passenger(s)
  - assign/change seat(s)
  - confirm or cancel reservation
- Persists data in PostgreSQL via JDBC repositories

## Current architecture (short version)

- `controllers/` contains business logic (`FlightController`, `ReservationController`)
- `repository/` contains interfaces + JDBC implementations
- `db/ConnectionFactory` provides DB connections from environment variables
- `integration/FlightComponentFacade` is the stable entrypoint for external consumers (Team T)
- `Application` wires production dependencies in one place

## Requirements

- Java 17
- Maven 3.9+
- PostgreSQL 14+ (or compatible)

## Environment variables

The app reads DB connection values from:

- `AIRLINE_DB_URL` (default: `jdbc:postgresql://localhost:5432/airline`)
- `AIRLINE_DB_USER` (default: `postgres`)
- `AIRLINE_DB_PASSWORD` (default: `postgres`)

If these are not set, defaults above are used.

## Database setup

Run schema first, then seed data:

```powershell
psql -U postgres -d airline -f src/main/resources/db/schema.sql
psql -U postgres -d airline -f src/main/resources/db/seed.sql
```

Notes:

- `seed.sql` is idempotent (safe to re-run).
- Seed data currently includes Icelandic airports only.

## Quick verification

Run tests:

```powershell
mvn test
```

Run minimal startup check (prints number of flights in storage):

- Run `airline.Application` from your IDE.

## Team T integration

Team T should use:

- `airline.integration.FlightComponentFacade`

Production instance:

```java
FlightComponentFacade facade = FlightComponentFacade.createProduction();
```

Detailed contract and behavior notes are documented here:

- `docs/team_t_integration_contract.md`

## Reservation flow (happy path)

1. Search flights
2. Build itinerary from selected flights
3. `createReservation(itinerary)`
4. `addPassenger(...)` for each traveler
5. `assignSeat(...)` for each passenger and each flight leg
6. `confirmReservation(...)`

## Booking/search rules currently enforced

- Search returns only flights with status `SCHEDULED` or `DELAYED`
- Seat ID must be `S<number>` and within flight capacity
- Same seat cannot be assigned twice on same flight
- Reservation can only be confirmed when every passenger has a seat assignment on every leg

