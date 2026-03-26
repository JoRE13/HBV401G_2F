package airline.repository;

import airline.model.Flight;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Secondary mock used to simulate a storage dependency that has no flights.
 * This supports bonus-style testing with multiple mock behaviors.
 */
public class EmptyFlightRepositoryMock implements FlightRepository {

    @Override
    public List<Flight> findAll() {
        return new ArrayList<>();
    }

    @Override
    public Flight findByFlightNumber(String flightNumber) {
        return null;
    }

    @Override
    public List<Flight> findByRouteAndDate(String departureCode, String arrivalCode, ZonedDateTime date) {
        return new ArrayList<>();
    }

    @Override
    public List<Flight> findByDepartureAirportAndDate(String airportCode, ZonedDateTime date) {
        return new ArrayList<>();
    }

    @Override
    public List<Flight> findDepartingFlights(String airportCode) {
        return new ArrayList<>();
    }

    @Override
    public List<Flight> findArrivingFlights(String airportCode) {
        return new ArrayList<>();
    }

    @Override
    public void save(Flight flight) {
        // Intentionally no-op: this mock always simulates empty storage.
    }

    @Override
    public void update(Flight flight) {
        // Intentionally no-op: this mock always simulates empty storage.
    }

    @Override
    public void delete(String flightNumber) {
        // Intentionally no-op: this mock always simulates empty storage.
    }
}
