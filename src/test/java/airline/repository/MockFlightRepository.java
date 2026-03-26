package airline.repository;

import airline.model.Flight;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock repository used by controller tests.
 * It intentionally keeps behavior minimal: only methods needed by current tests
 * are implemented with data, while unrelated methods are left unsupported.
 */
public class MockFlightRepository implements FlightRepository {
    private final List<Flight> flights = new ArrayList<>();

    // Returns a snapshot of mock storage data used by controller search logic.
    @Override
    public List<Flight> findAll() {
        return new ArrayList<>(flights);
    }

    @Override
    public Flight findByFlightNumber(String flightNumber) {
        throw unsupported();
    }

    @Override
    public List<Flight> findByRouteAndDate(String departureCode, String arrivalCode, ZonedDateTime date) {
        throw unsupported();
    }

    @Override
    public List<Flight> findByDepartureAirportAndDate(String airportCode, ZonedDateTime date) {
        throw unsupported();
    }

    @Override
    public List<Flight> findDepartingFlights(String airportCode) {
        throw unsupported();
    }

    @Override
    public List<Flight> findArrivingFlights(String airportCode) {
        throw unsupported();
    }

    // Saves fixture data so tests can control exactly which flights exist.
    @Override
    public void save(Flight flight) {
        flights.add(flight);
    }

    @Override
    public void update(Flight flight) {
        throw unsupported();
    }

    @Override
    public void delete(String flightNumber) {
        throw unsupported();
    }

    // Unused interface methods are intentionally unsupported to keep the mock minimal.
    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("Not needed for current controller tests");
    }
}
