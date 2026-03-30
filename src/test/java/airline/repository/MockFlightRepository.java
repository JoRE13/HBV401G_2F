package airline.repository;

import airline.model.Flight;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal mock implementation of {@link FlightRepository} for controller tests.
 *
 * <p>This mock does not implement real repository or database logic.
 * It only returns preconfigured values needed by tests.</p>
 */
public class MockFlightRepository implements FlightRepository {
    private List<Flight> routeAndDateResponse = new ArrayList<>();

    /**
     * Configures what findByRouteAndDate should return.
     *
     * @param flights flights to be returned by search calls
     */
    public void setRouteAndDateResponse(List<Flight> flights) {
        this.routeAndDateResponse = new ArrayList<>(flights);
    }

    /**
     * Returns preconfigured search response.
     * Parameters are intentionally ignored because this is a test stub.
     */
    @Override
    public List<Flight> findByRouteAndDate(String departureCode, String arrivalCode, ZonedDateTime date) {
        return new ArrayList<>(routeAndDateResponse);
    }

    @Override
    public List<Flight> findAll() {
        throw unsupported();
    }

    @Override
    public Flight findByFlightNumber(String flightNumber) {
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

    @Override
    public void save(Flight flight) {
        throw unsupported();
    }

    @Override
    public void update(Flight flight) {
        throw unsupported();
    }

    @Override
    public void delete(String flightNumber) {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("Not needed for current controller tests");
    }
}