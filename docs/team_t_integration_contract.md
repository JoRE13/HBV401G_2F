# Team F -> Team T Integration Contract (Flight Component)

This document describes how Team T should use Team F's flight component right now.

## 1) Integration entrypoint

Use this facade class:

- `airline.integration.FlightComponentFacade`

Create production instance:

```java
FlightComponentFacade facade = FlightComponentFacade.createProduction();
```

This gives Team T one stable place to call flight search + booking operations.

## 2) Available operations

### Search

- `searchFlights(String departureCode, String arrivalCode, ZonedDateTime date)`
- `searchFlights(String departureCode, String arrivalCode, ZonedDateTime date, int minAvailableSeats)`
- `searchFlightsInRange(String departureCode, String arrivalCode, ZonedDateTime start, ZonedDateTime end)`
- `searchFlightsInRange(String departureCode, String arrivalCode, ZonedDateTime start, ZonedDateTime end, int minAvailableSeats)`
- `searchByDepartureAirport(String airportCode, ZonedDateTime date)`
- `filterByDepartureTimeRange(List<Flight> flights, ZonedDateTime start, ZonedDateTime end)`
- `findConnectingItineraries(String fromCode, String toCode, ZonedDateTime date)`
- `findConnectingItinerariesInRange(String fromCode, String toCode, ZonedDateTime start, ZonedDateTime end)`
- `findConnectingItinerariesInRange(String fromCode, String toCode, ZonedDateTime start, ZonedDateTime end, int minAvailableSeats)`
- `getAvailableSeatCount(String flightNumber)`

### Booking

- `createReservation(Itinerary itinerary)`
- `addPassenger(String reservationCode, Passenger passenger)`
- `assignSeat(String reservationCode, String itemId, String flightNumber, String seatId)`
- `changeSeat(String reservationCode, String itemId, String flightNumber, String newSeatId)`
- `confirmReservation(String reservationCode)`
- `cancelReservation(String reservationCode)`
- `computeTotal(String reservationCode)`
- `viewReservationsByFlight(String flightNumber)`

## 3) Business rules Team T should expect

- Search returns only flights with status:
  - `SCHEDULED`
  - `DELAYED`
- Team T can request only flights with enough seats by using:
  - `searchFlights(..., minAvailableSeats)`
  - `searchFlightsInRange(..., minAvailableSeats)`
  - `findConnectingItinerariesInRange(..., minAvailableSeats)`
  - Example: if group size is 5, use `minAvailableSeats = 5`
- `searchFlights(...)` rejects null/blank airport codes and past dates.
- `searchFlightsInRange(...)` rejects null/blank airport codes, null dates, past dates, and start dates after end dates.
- Seat IDs must be in format `S<number>` and within flight capacity.
  - Example: `S12`
  - If a flight has capacity 72, valid range is `S1` to `S72`.
- Same seat cannot be assigned twice on the same flight.
- Reservation can only be confirmed if:
  - reservation is still `PENDING`
  - it has passengers/items
  - every passenger item has a seat assignment for every flight leg.

## 4) Error behavior

Current implementation throws runtime exceptions for invalid operations:

- `IllegalArgumentException` for invalid input / missing entities.
- `IllegalStateException` for invalid workflow state (for example confirm before all seats are assigned).

Team T should handle those exceptions and map them to user-facing messages.

## 5) Minimal usage flow (happy path)

1. Search flights.
   - If searching for groups, use `searchFlights(..., minAvailableSeats)`.
   - If searching across multiple days, use `searchFlightsInRange(...)`.
   - If searching for connecting flights across multiple days, use `findConnectingItinerariesInRange(...)`.
2. Build itinerary from selected flights.
3. `createReservation(itinerary)` -> get `reservationCode`.
4. For each traveler: `addPassenger(reservationCode, passenger)` -> get `itemId`.
5. For each traveler and each flight leg: `assignSeat(...)`.
6. `confirmReservation(reservationCode)`.

## 6) Environment requirements

- PostgreSQL must be available.
- Schema must be applied from:
  - `src/main/resources/db/schema.sql`
- Seed data can be loaded from:
  - `src/main/resources/db/seed.sql`
- Connection values come from environment variables:
  - `AIRLINE_DB_URL`
  - `AIRLINE_DB_USER`
  - `AIRLINE_DB_PASSWORD`

### Team T setup commands (Windows PowerShell)

Run from project root:

```powershell
# Create DB once
psql -U postgres -c "CREATE DATABASE airline;"

# Apply schema and seed
psql -U postgres -d airline -f src/main/resources/db/schema.sql
psql -U postgres -d airline -f src/main/resources/db/seed.sql

# Set env vars for current terminal
$env:AIRLINE_DB_URL="jdbc:postgresql://localhost:5432/airline"
$env:AIRLINE_DB_USER="postgres"
$env:AIRLINE_DB_PASSWORD="postgres"
```
