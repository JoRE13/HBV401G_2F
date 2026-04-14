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
 * Stable integration entrypoint for external consumers (for example Team T).
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
                components.getReservationController()
        );
    }

    // Search API

    public List<Flight> searchFlights(String departureCode, String arrivalCode, ZonedDateTime date) {
        return flightController.searchFlights(departureCode, arrivalCode, date);
    }

    public List<Flight> searchFlights(
            String departureCode,
            String arrivalCode,
            ZonedDateTime date,
            int minAvailableSeats) {
        return flightController.searchFlights(departureCode, arrivalCode, date, minAvailableSeats);
    }

    public List<Flight> searchByDepartureAirport(String airportCode, ZonedDateTime date) {
        return flightController.searchByDepartureAirport(airportCode, date);
    }

    public List<Flight> filterByDepartureTimeRange(List<Flight> flights, ZonedDateTime start, ZonedDateTime end) {
        return flightController.filterByDepartureTimeRange(flights, start, end);
    }

    public List<Itinerary> findConnectingItineraries(String fromCode, String toCode, ZonedDateTime date) {
        return flightController.findConnectingItineraries(fromCode, toCode, date);
    }

    public int getAvailableSeatCount(String flightNumber) {
        return flightController.getAvailableSeatCount(flightNumber);
    }

    // Booking API

    public Reservation createReservation(Itinerary itinerary) {
        return reservationController.createReservation(itinerary);
    }

    public ReservationItem addPassenger(String reservationCode, Passenger passenger) {
        return reservationController.addPassenger(reservationCode, passenger);
    }

    public void assignSeat(String reservationCode, String itemId, String flightNumber, String seatId) {
        reservationController.assignSeat(reservationCode, itemId, flightNumber, seatId);
    }

    public void changeSeat(String reservationCode, String itemId, String flightNumber, String newSeatId) {
        reservationController.changeSeat(reservationCode, itemId, flightNumber, newSeatId);
    }

    public void confirmReservation(String reservationCode) {
        reservationController.confirmReservation(reservationCode);
    }

    public void cancelReservation(String reservationCode) {
        reservationController.cancelReservation(reservationCode);
    }

    public double computeTotal(String reservationCode) {
        return reservationController.computeTotal(reservationCode);
    }

    public List<Reservation> viewReservationsByFlight(String flightNumber) {
        return reservationController.viewReservationsByFlight(flightNumber);
    }
}
