package airline.repository;

import airline.model.Flight;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A lightweight in-memory mock implementation of {@link FlightRepository}
 * used exclusively for controller-level tests.
 *
 * <p>This mock stores {@link Flight} objects in a simple list and provides
 * only the minimal behavior required by the tests. It is <b>not</b> intended
 * to fully replicate repository or database behavior.</p>
 *
 * <p><b>Supported operations:</b>
 * <ul>
 *   <li>{@link #findAll()} – returns all stored flights</li>
 *   <li>{@link #findByRouteAndDate(String, String, ZonedDateTime)} – filters flights by route and date</li>
 *   <li>{@link #save(Flight)} – adds flights to the in-memory store</li>
 * </ul>
 *
 * <p><b>Unsupported operations:</b>
 * <ul>
 *   <li>All other methods throw {@link UnsupportedOperationException}</li>
 * </ul>
 *
 * <p>This design keeps tests focused and avoids unnecessary complexity.</p>
 */
public class MockFlightRepository implements FlightRepository {
    private final List<Flight> flights = new ArrayList<>();

    /**
     * Returns a copy of all flights currently stored in the mock repository.
     *
     * @return list of all mock flights
     */
    @Override
    public List<Flight> findAll() {
        return new ArrayList<>(flights);
    }

    @Override
    public Flight findByFlightNumber(String flightNumber) {
        throw unsupported();
    }

    /**
     * Returns flights that match the given departure airport, arrival airport,
     * and departure date.
     *
     * <p>The comparison is based on:
     * <ul>
     *   <li>Exact airport code match</li>
     *   <li>Date only (time component is ignored)</li>
     * </ul>
     *
     * @param departureCode code of departure airport
     * @param arrivalCode code of arrival airport
     * @param date date to match (time ignored)
     * @return list of matching flights, or empty list if none match
     */
    @Override
    public List<Flight> findByRouteAndDate(String departureCode, String arrivalCode, ZonedDateTime date) {
        List<Flight> result = new ArrayList<>();
        for (Flight flight : flights) {
            if (flight.getDepartureAirport().getAirportCode().equals(departureCode)
                    && flight.getArrivalAirport().getAirportCode().equals(arrivalCode)
                    && flight.getDepartureDateTime().toLocalDate().equals(date.toLocalDate())) {
                result.add(flight);
            }
        }
        return result;
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

    /**
     * Adds a flight to the in-memory repository.
     *
     * <p>This method is used by tests to set up fixture data.</p>
     *
     * @param flight flight to store
     */
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

    /**
     * Helper method used to indicate that a repository operation
     * is not required for current tests.
     *
     * @return UnsupportedOperationException always thrown by unsupported methods
     */
    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException("Not needed for current controller tests");
    }
}
