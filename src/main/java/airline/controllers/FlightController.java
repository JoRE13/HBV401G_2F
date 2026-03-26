package airline.controllers;

import airline.model.Flight;
import airline.model.Itinerary;
import airline.repository.FlightRepository;
import airline.repository.InMemoryFlightRepository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FlightController {
    private final FlightRepository flightRepository;

    public FlightController() {
        this(new InMemoryFlightRepository());
    }

    public FlightController(FlightRepository flightRepository) {
        if (flightRepository == null) {
            throw new IllegalArgumentException("flightRepository cannot be null");
        }
        this.flightRepository = flightRepository;
    }

    // get departing and arriving flights

    public List<Flight> getDepartingFlights(String airportCode) {
        return flightRepository.findDepartingFlights(airportCode);
    }

    public List<Flight> getArrivingFlights(String airportCode) {
        return flightRepository.findArrivingFlights(airportCode);
    }

    // search methods
    public List<Flight> searchFlights(
            String departureCode,
            String arrivalCode,
            ZonedDateTime date) {
        if (departureCode == null || departureCode.isBlank()) {
            throw new IllegalArgumentException("departureCode cannot be null or blank");
        }
        if (arrivalCode == null || arrivalCode.isBlank()) {
            throw new IllegalArgumentException("arrivalCode cannot be null or blank");
        }
        if (date == null) {
            throw new IllegalArgumentException("date cannot be null");
        }

        List<Flight> search = new ArrayList<>();
        for (Flight flight : flightRepository.findAll()) {
            if (flight.getDepartureAirport().getAirportCode().equals(departureCode)
                    && flight.getArrivalAirport().getAirportCode().equals(arrivalCode)
                    && flight.getDepartureDateTime().toLocalDate().equals(date.toLocalDate())) {
                search.add(flight);
            }
        }
        return search;
    }

    public List<Flight> searchByDepartureAirport(
            String airportCode,
            ZonedDateTime date) {
        return flightRepository.findByDepartureAirportAndDate(airportCode, date);
    }

    public List<Flight> filterByDepartureTimeRange(
            List<Flight> inputFlights,
            ZonedDateTime start,
            ZonedDateTime end) {
        if (inputFlights == null) {
            throw new IllegalArgumentException("inputFlights cannot be null");
        }
        if (start == null || end == null) {
            throw new IllegalArgumentException("start and end cannot be null");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("start cannot be after end");
        }

        List<Flight> filterResult = new ArrayList<>();
        for (Flight f : inputFlights) {
            ZonedDateTime departure = f.getDepartureDateTime();
            if (departure.isAfter(start) && departure.isBefore(end)) {
                filterResult.add(f);
            }
        }
        return filterResult;
    }

    public List<Flight> sortByPrice(List<Flight> flights) {
        flights.sort(Comparator.comparingDouble(Flight::getBasePrice));
        return flights;
    }

    public List<Itinerary> findConnectingItineraries(
            String fromCode,
            String toCode,
            ZonedDateTime date) {
        List<Itinerary> result = new ArrayList<>();
        List<Flight> flights = flightRepository.findAll();

        for (Flight f1 : flights) {
            if (!f1.getDepartureAirport().getAirportCode().equals(fromCode))
                continue;

            for (Flight f2 : flights) {
                if (f1.getArrivalAirport().getAirportCode()
                        .equals(f2.getDepartureAirport().getAirportCode())
                        && f2.getArrivalAirport().getAirportCode().equals(toCode)) {

                    List<Flight> legs = new ArrayList<>();
                    legs.add(f1);
                    legs.add(f2);

                    Itinerary itin = new Itinerary(
                            (f1.getFlightNumber() + " - " + f2.getFlightNumber()),
                            0,
                            0,
                            legs);

                    itin.computeTotalDuration();
                    itin.computeTotalPrice();

                    result.add(itin);
                }
            }
        }
        return result;
    }

    // Flug-valkostir
    public void addFlight(Flight flight) {
        flightRepository.save(flight);
    }

    public void updateFlight(Flight flight) {
        flightRepository.update(flight);
    }

    public void removeFlight(String flightNumber) {
        flightRepository.delete(flightNumber);
    }

    public void cancelFlight(String flightNumber) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber);
        if (flight != null) {
            flight.setStatusCancelled();
            flightRepository.update(flight);
        }
    }

    public void rescheduleFlight(
            String flightNumber,
            ZonedDateTime newDepartureTime,
            ZonedDateTime newArrivalTime) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber);
        if (flight != null) {
            flight.reschedule(newDepartureTime, newArrivalTime);
            flightRepository.update(flight);
        }
    }

    public static void main(String[] args) {

    }

}
