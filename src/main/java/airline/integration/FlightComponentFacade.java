package airline.integration;

import airline.Application;
import airline.controllers.FlightController;
import airline.controllers.ReservationController;
import airline.model.Flight;
import airline.model.Itinerary;
import airline.model.Passenger;
import airline.model.Reservation;
import airline.model.ReservationItem;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Stable integration entrypoint for external consumers (for Team T).
 *
 * Team T should call this facade instead of reaching into repositories or
 * controller internals directly.
 */
public class FlightComponentFacade {
    private final FlightController flightController;
    private final ReservationController reservationController;

    public FlightComponentFacade(
            FlightController flightController,
            ReservationController reservationController) {
        if (flightController == null) {
            throw new IllegalArgumentException("flightController cannot be null");
        }
        if (reservationController == null) {
            throw new IllegalArgumentException("reservationController cannot be null");
        }
        this.flightController = flightController;
        this.reservationController = reservationController;
    }

    /**
     * Creates a facade backed by production PostgreSQL repositories.
     */
    public static FlightComponentFacade createProduction() {
        Application.Components components = Application.createProductionComponents();
        return new FlightComponentFacade(
                components.getFlightController(),
                components.getReservationController());
    }

    /**
     * Searches flights by route and departure date.
     */
    public List<Flight> searchFlights(String departureCode, String arrivalCode, ZonedDateTime date) {
        return flightController.searchFlights(departureCode, arrivalCode, date);
    }

    /**
     * Route/date flight search with minimum availability requirement.
     */
    public List<Flight> searchFlights(
            String departureCode,
            String arrivalCode,
            ZonedDateTime date,
            int minAvailableSeats) {
        return flightController.searchFlights(departureCode, arrivalCode, date, minAvailableSeats);
    }

    /**
     * Searches flights by route and departure dateRange.
     */
    public List<Flight> searchFlightsInRange(String departureCode, String arrivalCode, ZonedDateTime start,
            ZonedDateTime end) {
        return flightController.searchFlightsInRange(departureCode, arrivalCode, start, end);
    }

    /**
     * Route/date flight search with minimum availability requirement.
     */
    public List<Flight> searchFlightsInRange(
            String departureCode,
            String arrivalCode,
            ZonedDateTime start,
            ZonedDateTime end,
            int minAvailableSeats) {
        return flightController.searchFlightsInRange(departureCode, arrivalCode, start, end, minAvailableSeats);
    }

    /**
     * Searches by departure airport and date.
     */
    public List<Flight> searchByDepartureAirport(String airportCode, ZonedDateTime date) {
        return flightController.searchByDepartureAirport(airportCode, date);
    }

    /**
     * Filters a list of flights by an inclusive day range.
     */
    public List<Flight> filterByDepartureTimeRange(List<Flight> flights, ZonedDateTime start, ZonedDateTime end) {
        return flightController.filterByDepartureTimeRange(flights, start, end);
    }

    /**
     * Finds connecting itineraries (currently two-leg itineraries).
     */
    public List<Itinerary> findConnectingItineraries(String fromCode, String toCode, ZonedDateTime date) {
        return flightController.findConnectingItineraries(fromCode, toCode, date);
    }

    /**
     * Finds connecting itineraries across an inclusive departure day range.
     */
    public List<Itinerary> findConnectingItinerariesInRange(
            String fromCode,
            String toCode,
            ZonedDateTime start,
            ZonedDateTime end) {
        return flightController.findConnectingItinerariesInRange(fromCode, toCode, start, end);
    }

    /**
     * Finds connecting itineraries across an inclusive departure day range with
     * a minimum availability requirement on each leg.
     */
    public List<Itinerary> findConnectingItinerariesInRange(
            String fromCode,
            String toCode,
            ZonedDateTime start,
            ZonedDateTime end,
            int minAvailableSeats) {
        return flightController.findConnectingItinerariesInRange(
                fromCode,
                toCode,
                start,
                end,
                minAvailableSeats
        );
    }

    /**
     * Returns currently available seats for a flight.
     */
    public int getAvailableSeatCount(String flightNumber) {
        return flightController.getAvailableSeatCount(flightNumber);
    }

    /**
     * Creates a reservation for the given itinerary.
     */
    public Reservation createReservation(Itinerary itinerary) {
        return reservationController.createReservation(itinerary);
    }

    /**
     * Adds one passenger to an existing reservation.
     */
    public ReservationItem addPassenger(String reservationCode, Passenger passenger) {
        return reservationController.addPassenger(reservationCode, passenger);
    }

    /**
     * Assigns seat for a passenger item on a flight leg.
     */
    public void assignSeat(String reservationCode, String itemId, String flightNumber, String seatId) {
        reservationController.assignSeat(reservationCode, itemId, flightNumber, seatId);
    }

    /**
     * Changes seat assignment for a passenger item on a flight leg.
     */
    public void changeSeat(String reservationCode, String itemId, String flightNumber, String newSeatId) {
        reservationController.changeSeat(reservationCode, itemId, flightNumber, newSeatId);
    }

    /**
     * Confirms reservation after validation checks pass.
     */
    public void confirmReservation(String reservationCode) {
        reservationController.confirmReservation(reservationCode);
    }

    /**
     * Cancels a reservation.
     */
    public void cancelReservation(String reservationCode) {
        reservationController.cancelReservation(reservationCode);
    }

    /**
     * Recomputes reservation total from reservation items.
     */
    public double computeTotal(String reservationCode) {
        return reservationController.computeTotal(reservationCode);
    }

    /**
     * Lists reservations linked to a specific flight.
     */
    public List<Reservation> viewReservationsByFlight(String flightNumber) {
        return reservationController.viewReservationsByFlight(flightNumber);
    }
}
