package airline.repository;

import airline.model.Flight;

import java.time.ZonedDateTime;
import java.util.List;

public interface FlightRepository {

    List<Flight> findAll();

    Flight findByFlightNumber(String flightNumber);

    List<Flight> findByRouteAndDate(String departureCode, String arrivalCode, ZonedDateTime date);

    List<Flight> findByDepartureAirportAndDate(String airportCode, ZonedDateTime date);

    List<Flight> findDepartingFlights(String airportCode);

    List<Flight> findArrivingFlights(String airportCode);

    void save(Flight flight);

    void update(Flight flight);

    void delete(String flightNumber);
}
