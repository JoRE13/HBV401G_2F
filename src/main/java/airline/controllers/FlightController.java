package airline.controllers;

import airline.model.Flight;
import airline.model.FlightStatus;
import airline.model.Itinerary;
import airline.repository.FlightRepository;
import airline.repository.InMemoryFlightRepository;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Controller for flight search and flight management use cases.
 *
 * The controller validates incoming input and delegates storage operations to FlightRepository. 
 * Search methods return only bookable flights
 * (scheduled or delayed).
 */
public class FlightController {
    private final FlightRepository flightRepository;

    /**
     * Creates a controller backed by the in-memory repository.
     *
     * This constructor is convenient for local/manual usage. For production and
     * tests, prefer dependency injection through FlightController(FlightRepository).
     */
    public FlightController() {
        this(new InMemoryFlightRepository());
    }

    /**
     * Creates a controller with an explicit repository dependency.
     *
     * @param flightRepository repository used for flight queries and persistence
     */
    public FlightController(FlightRepository flightRepository) {
        if (flightRepository == null) {
            throw new IllegalArgumentException("flightRepository cannot be null");
        }
        this.flightRepository = flightRepository;
    }

    /**
     * Returns flights that depart from the given airport code.
     */
    public List<Flight> getDepartingFlights(String airportCode) {
        return flightRepository.findDepartingFlights(airportCode);
    }

    /**
     * Returns flights that arrive at the given airport code.
     */
    public List<Flight> getArrivingFlights(String airportCode) {
        return flightRepository.findArrivingFlights(airportCode);
    }

    /**
     * Searches flights by route and departure date.
     *
     * The result contains only bookable flights (scheduled or delayed).
     */
    public List<Flight> searchFlights(
            String departureCode,
            String arrivalCode,
            ZonedDateTime date) {
        ZoneId zone = ZoneId.of("UTC");
        if (departureCode == null || departureCode.isBlank()) {
            throw new IllegalArgumentException("departureCode cannot be null or blank");
        }
        if (arrivalCode == null || arrivalCode.isBlank()) {
            throw new IllegalArgumentException("arrivalCode cannot be null or blank");
        }
        if (date == null || date.toLocalDate().isBefore(ZonedDateTime.now(zone).toLocalDate())) {
            throw new IllegalArgumentException("date cannot be null and date cannot be in the past");
        }

        List<Flight> candidates = flightRepository.findByRouteAndDate(departureCode, arrivalCode, date);
        return filterBookableFlights(candidates);
    }

    /**
     * Route/date search with a minimum seat availability constraint.
     */
    public List<Flight> searchFlights(
            String departureCode,
            String arrivalCode,
            ZonedDateTime date,
            int minAvailableSeats) {
        if (minAvailableSeats < 1) {
            throw new IllegalArgumentException("minAvailableSeats must be at least 1");
        }
        List<Flight> candidates = searchFlights(departureCode, arrivalCode, date);
        return filterByMinimumAvailableSeats(candidates, minAvailableSeats);
    }

    /**
     * Searches bookable flights by departure airport and date.
     */
    public List<Flight> searchByDepartureAirport(
            String airportCode,
            ZonedDateTime date) {
        List<Flight> candidates = flightRepository.findByDepartureAirportAndDate(airportCode, date);
        return filterBookableFlights(candidates);
    }

    /**
     * Returns currently available seat count for a specific flight.
     */
    public int getAvailableSeatCount(String flightNumber) {
        if (flightNumber == null || flightNumber.isBlank()) {
            throw new IllegalArgumentException("flightNumber cannot be null or blank");
        }
        return flightRepository.findAvailableSeatCount(flightNumber);
    }

    /**
     * Filters flights by departure day range.
     *
     * The range is inclusive by date (start day through end day). Internally it
     * is implemented as [start-at-00:00, end+1day-at-00:00).
     */
    public List<Flight> filterByDepartureTimeRange(
            List<Flight> inputFlights,
            ZonedDateTime start,
            ZonedDateTime end) {
        ZoneId zone = ZoneId.of("UTC");
        if (inputFlights == null) {
            throw new IllegalArgumentException("inputFlights cannot be null");
        }
        if (start == null || end == null) {
            throw new IllegalArgumentException("start and end cannot be null");
        }
        if (start.toLocalDate().isAfter(end.toLocalDate())) {
            throw new IllegalArgumentException("start cannot be after end");
        }
        if (start.toLocalDate().isBefore(ZonedDateTime.now(zone).toLocalDate())){
            throw new IllegalArgumentException("dates cannot be in the past");
        }

        ZonedDateTime rangeStart = start.toLocalDate().atStartOfDay(start.getZone());
        ZonedDateTime rangeEndExclusive = end.toLocalDate().plusDays(1).atStartOfDay(end.getZone());

        List<Flight> filterResult = new ArrayList<>();
        for (Flight f : inputFlights) {
            ZonedDateTime departure = f.getDepartureDateTime();
            if (!departure.isBefore(rangeStart) && departure.isBefore(rangeEndExclusive)) {
                filterResult.add(f);
            }
        }
        return filterResult;
    }

    /**
     * Sorts flights by base price in ascending order.
     *
     * Note: this mutates the provided list.
     */
    public List<Flight> sortByPrice(List<Flight> flights) {
        flights.sort(Comparator.comparingDouble(Flight::getBasePrice));
        return flights;
    }

    /**
     * Finds two-leg connecting itineraries for a given route.
     */
    public List<Itinerary> findConnectingItineraries(
            String fromCode,
            String toCode,
            ZonedDateTime date) {
        List<Itinerary> result = new ArrayList<>();
        List<Flight> flights = flightRepository.findAll();

        for (Flight f1 : flights) {
            if (!isBookableForSearch(f1)) {
                continue;
            }
            if (!f1.getDepartureAirport().getAirportCode().equals(fromCode))
                continue;

            for (Flight f2 : flights) {
                if (!isBookableForSearch(f2)) {
                    continue;
                }
                if (f1.getArrivalAirport().getAirportCode()
                        .equals(f2.getDepartureAirport().getAirportCode())
                        && f2.getArrivalAirport().getAirportCode().equals(toCode) && f1.getArrivalDateTime().isBefore(f2.getDepartureDateTime())) {

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

    /**
     * Persists a new flight.
     */
    public void addFlight(Flight flight) {
        flightRepository.save(flight);
    }

    /**
     * Persists updates to an existing flight.
     */
    public void updateFlight(Flight flight) {
        flightRepository.update(flight);
    }

    /**
     * Deletes a flight by flight number.
     */
    public void removeFlight(String flightNumber) {
        flightRepository.delete(flightNumber);
    }

    /**
     * Cancels a flight by setting its status to cancelled and saving the change.
     */
    public void cancelFlight(String flightNumber) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber);
        if (flight != null) {
            flight.setStatusCancelled();
            flightRepository.update(flight);
        }
        else {
            throw new IllegalArgumentException("Flight not found: " + flightNumber);
        }
    }

    /**
     * Reschedules a flight and persists the updated departure/arrival times.
     */
    public void rescheduleFlight(
            String flightNumber,
            ZonedDateTime newDepartureTime,
            ZonedDateTime newArrivalTime) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber);
        if (flight != null) {
            flight.reschedule(newDepartureTime, newArrivalTime);
            flightRepository.update(flight);
        }
        else {
            throw new IllegalArgumentException("Flight not found: " + flightNumber);
        }
    }

    public static void main(String[] args) {

    }

    private List<Flight> filterBookableFlights(List<Flight> flights) {
        List<Flight> filtered = new ArrayList<>();
        for (Flight flight : flights) {
            if (isBookableForSearch(flight)) {
                filtered.add(flight);
            }
        }
        return filtered;
    }

    private List<Flight> filterByMinimumAvailableSeats(List<Flight> flights, int minAvailableSeats) {
        List<Flight> filtered = new ArrayList<>();
        for (Flight flight : flights) {
            int availableSeats = flightRepository.findAvailableSeatCount(flight.getFlightNumber());
            if (availableSeats >= minAvailableSeats) {
                filtered.add(flight);
            }
        }
        return filtered;
    }

    private boolean isBookableForSearch(Flight flight) {
        if (flight == null) {
            return false;
        }
        return flight.getStatus() == FlightStatus.SCHEDULED
                || flight.getStatus() == FlightStatus.DELAYED;
    }

}
