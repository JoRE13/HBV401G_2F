package airline.repository;

import airline.model.Flight;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A mock implementation of {@link FlightRepository} that simulates an
 * always-empty data source.
 *
 * <p>This mock is used in tests that verify behavior when no flights exist,
 * such as ensuring controllers return empty results or handle missing data
 * correctly.</p>
 *
 * <p>All query methods return empty collections (or {@code null} where applicable),
 * and all mutation methods ({@code save}, {@code update}, {@code delete})
 * are implemented as no-ops.</p>
 *
 * <p>This mock is intentionally stateless and ignores all inputs.</p>
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
