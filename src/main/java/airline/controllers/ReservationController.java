package airline.controllers;

import airline.model.Flight;
import airline.model.Itinerary;
import airline.model.Passenger;
import airline.model.Reservation;
import airline.model.ReservationItem;
import airline.model.ReservationStatus;
import airline.repository.FlightRepository;
import airline.repository.PassengerRepository;
import airline.repository.ReservationRepository;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Reservation workflow controller wired to storage repositories.
 */
public class ReservationController {
    private final ReservationRepository reservationRepository;
    private final PassengerRepository passengerRepository;
    private final FlightRepository flightRepository;

    public ReservationController(
            ReservationRepository reservationRepository,
            PassengerRepository passengerRepository,
            FlightRepository flightRepository) {
        if (reservationRepository == null) {
            throw new IllegalArgumentException("reservationRepository cannot be null");
        }
        if (passengerRepository == null) {
            throw new IllegalArgumentException("passengerRepository cannot be null");
        }
        if (flightRepository == null) {
            throw new IllegalArgumentException("flightRepository cannot be null");
        }
        this.reservationRepository = reservationRepository;
        this.passengerRepository = passengerRepository;
        this.flightRepository = flightRepository;
    }

    /**
     * Creates a pending reservation for a selected itinerary and links all
     * itinerary flight legs to the reservation.
     */
    public Reservation createReservation(Itinerary itinerary) {
        if (itinerary == null) {
            throw new IllegalArgumentException("itinerary cannot be null");
        }
        double total = itinerary.computeTotalPrice();
        Reservation reservation = new Reservation(
                "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                new Date(),
                null,
                total
        );
        reservationRepository.save(reservation);
        for (Flight leg : itinerary.getLegs()) {
            reservationRepository.linkFlight(reservation.getReservationCode(), leg.getFlightNumber());
        }
        return reservation;
    }

    /**
     * Adds one passenger to an existing pending reservation and creates the
     * matching reservation item.
     */
    public ReservationItem addPassenger(String reservationCode, Passenger passenger) {
        if (reservationCode == null || reservationCode.isBlank()) {
            throw new IllegalArgumentException("reservationCode cannot be null or blank");
        }
        if (passenger == null) {
            throw new IllegalArgumentException("passenger cannot be null");
        }
        if (passenger.getEmail() == null || passenger.getEmail().isBlank()) {
            throw new IllegalArgumentException("passenger email cannot be null or blank");
        }
        Reservation reservation = requireReservation(reservationCode);
        requirePending(reservation, "add passenger");
        passengerRepository.save(passenger);

        List<String> flightNumbers = reservationRepository.findFlightNumbersByReservation(reservationCode);
        if (flightNumbers.isEmpty()) {
            throw new IllegalStateException("Reservation has no linked flights: " + reservationCode);
        }

        double passengerPrice = 0.0;
        for (String flightNumber : flightNumbers) {
            Flight flight = flightRepository.findByFlightNumber(flightNumber);
            if (flight == null) {
                throw new IllegalStateException(
                        "Linked flight not found while pricing reservation " + reservationCode + ": " + flightNumber
                );
            }
            passengerPrice += flight.getBasePrice();
        }

        ReservationItem item = new ReservationItem(
                "ITEM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                passengerPrice,
                passenger.getEmail()
        );
        reservationRepository.saveItem(reservationCode, item);
        computeTotal(reservationCode);
        return item;
    }

    /**
     * Assigns (or reassigns) a seat for one passenger item on one flight leg.
     */
    public void assignSeat(String reservationCode, String itemId, String flightNumber, String seatId) {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("itemId cannot be null or blank");
        }
        if (flightNumber == null || flightNumber.isBlank()) {
            throw new IllegalArgumentException("flightNumber cannot be null or blank");
        }
        if (seatId == null || seatId.isBlank()) {
            throw new IllegalArgumentException("seatId cannot be null or blank");
        }
        Reservation reservation = requireReservation(reservationCode);
        requirePending(reservation, "assign seat");
        String normalizedSeatId = normalizeAndValidateSeatId(flightNumber, seatId);
        reservationRepository.assignSeat(reservationCode, itemId, flightNumber, normalizedSeatId);
    }

    /**
     * Confirms a reservation.
     *
     * All passengers must have seat assignments on all linked flight legs before
     * confirmation is allowed.
     */
    public void confirmReservation(String reservationCode) {
        Reservation reservation = requireReservation(reservationCode);
        requirePending(reservation, "confirm reservation");
        List<ReservationItem> items = reservationRepository.findItemsByReservation(reservationCode);
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot confirm reservation without passengers/items: " + reservationCode);
        }
        List<String> flightNumbers = reservationRepository.findFlightNumbersByReservation(reservationCode);
        if (flightNumbers.isEmpty()) {
            throw new IllegalStateException("Cannot confirm reservation without flight legs: " + reservationCode);
        }

        for (ReservationItem item : items) {
            for (String flightNumber : flightNumbers) {
                boolean assigned = reservationRepository.hasSeatAssignment(
                        reservationCode,
                        item.getItemId(),
                        flightNumber
                );
                if (!assigned) {
                    throw new IllegalStateException(
                            "Cannot confirm reservation; missing seat assignment for item "
                                    + item.getItemId() + " on flight " + flightNumber
                    );
                }
            }
        }

        reservation.confirmReservation();
        reservationRepository.update(reservation);
    }

    /**
     * Cancels a pending reservation.
     */
    public void cancelReservation(String reservationCode) {
        Reservation reservation = requireReservation(reservationCode);
        requirePending(reservation, "cancel reservation");
        reservation.cancelReservation();
        reservationRepository.update(reservation);
    }

    /**
     * Convenience wrapper for changing a seat assignment.
     */
    public void changeSeat(String reservationCode, String itemId, String flightNumber, String newSeatId) {
        if (newSeatId == null || newSeatId.isBlank()) {
            throw new IllegalArgumentException("newSeatId cannot be null or blank");
        }
        assignSeat(reservationCode, itemId, flightNumber, newSeatId);
    }

    /**
     * Recomputes and persists reservation total from reservation items.
     *
     * @return updated total price
     */
    public double computeTotal(String reservationCode) {
        Reservation reservation = requireReservation(reservationCode);
        List<ReservationItem> items = reservationRepository.findItemsByReservation(reservationCode);
        double total = 0.0;
        for (ReservationItem item : items) {
            total += item.getPricePaid();
        }
        reservation.setTotalPrice(total);
        reservationRepository.update(reservation);
        return total;
    }

    /**
     * Returns all reservations that include a given flight number.
     */
    public List<Reservation> viewReservationsByFlight(String flightNumber) {
        if (flightNumber == null || flightNumber.isBlank()) {
            throw new IllegalArgumentException("flightNumber cannot be null or blank");
        }
        if (flightRepository.findByFlightNumber(flightNumber) == null) {
            throw new IllegalArgumentException("Flight not found: " + flightNumber);
        }
        return reservationRepository.findByFlight(flightNumber);
    }

    // Compatibility alias while migrating callers to UML naming.
    public List<Reservation> listReservationsByFlight(String flightNumber) {
        return viewReservationsByFlight(flightNumber);
    }

    private Reservation requireReservation(String reservationCode) {
        if (reservationCode == null || reservationCode.isBlank()) {
            throw new IllegalArgumentException("reservationCode cannot be null or blank");
        }
        Reservation reservation = reservationRepository.findByCode(reservationCode);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found: " + reservationCode);
        }
        return reservation;
    }

    private void requirePending(Reservation reservation, String action) {
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot " + action + " when reservation status is " + reservation.getStatus()
            );
        }
    }

    private String normalizeAndValidateSeatId(String flightNumber, String seatId) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber);
        if (flight == null) {
            throw new IllegalArgumentException("Flight not found: " + flightNumber);
        }

        String normalized = seatId.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() < 2 || normalized.charAt(0) != 'S') {
            throw new IllegalArgumentException("seatId must be in format S<number>, for example S12");
        }

        int seatNumber;
        try {
            seatNumber = Integer.parseInt(normalized.substring(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("seatId must be in format S<number>, for example S12");
        }

        if (seatNumber < 1 || seatNumber > flight.getCapacity()) {
            throw new IllegalArgumentException(
                    "seatId out of range for flight " + flightNumber + ": "
                            + normalized + " (capacity " + flight.getCapacity() + ")"
            );
        }
        return normalized;
    }
}
