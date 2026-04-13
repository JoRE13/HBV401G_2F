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
- `searchByDepartureAirport(String airportCode, ZonedDateTime date)`
- `filterByDepartureTimeRange(List<Flight> flights, ZonedDateTime start, ZonedDateTime end)`
- `findConnectingItineraries(String fromCode, String toCode, ZonedDateTime date)`

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
- `searchFlights(...)` rejects null/blank airport codes and past dates.
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
