package airline.repository;

import airline.model.Flight;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class InMemoryFlightRepository implements FlightRepository {
    private final List<Flight> flights;

    public InMemoryFlightRepository() {
        this.flights = new ArrayList<>();
    }

    @Override
    public List<Flight> findAll() {
        return new ArrayList<>(flights);
    }

    @Override
    public Flight findByFlightNumber(String flightNumber) {
        for (Flight flight : flights) {
            if (flight.getFlightNumber().equals(flightNumber)) {
                return flight;
            }
        }
        return null;
    }

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
        List<Flight> result = new ArrayList<>();
        for (Flight flight : flights) {
            if (flight.getDepartureAirport().getAirportCode().equals(airportCode)
                    && flight.getDepartureDateTime().toLocalDate().equals(date.toLocalDate())) {
                result.add(flight);
            }
        }
        return result;
    }

    @Override
    public List<Flight> findDepartingFlights(String airportCode) {
        List<Flight> result = new ArrayList<>();
        for (Flight flight : flights) {
            if (flight.getDepartureAirport().getAirportCode().equals(airportCode)) {
                result.add(flight);
            }
        }
        return result;
    }

    @Override
    public List<Flight> findArrivingFlights(String airportCode) {
        List<Flight> result = new ArrayList<>();
        for (Flight flight : flights) {
            if (flight.getArrivalAirport().getAirportCode().equals(airportCode)) {
                result.add(flight);
            }
        }
        return result;
    }

    @Override
    public void save(Flight flight) {
        flights.add(flight);
    }

    @Override
    public void update(Flight flight) {
        delete(flight.getFlightNumber());
        flights.add(flight);
    }

    @Override
    public void delete(String flightNumber) {
        flights.removeIf(flight -> flight.getFlightNumber().equals(flightNumber));
    }
}
