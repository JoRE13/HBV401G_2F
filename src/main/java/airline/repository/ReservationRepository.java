package airline.repository;

import airline.model.Reservation;
import airline.model.ReservationItem;

import java.util.List;

public interface ReservationRepository {
    void save(Reservation reservation);

    void update(Reservation reservation);

    Reservation findByCode(String reservationCode);

    List<Reservation> findByFlight(String flightNumber);

    void linkFlight(String reservationCode, String flightNumber);

    void saveItem(String reservationCode, ReservationItem item);

    void assignSeat(String reservationCode, String itemId, String flightNumber, String seatId);

    List<ReservationItem> findItemsByReservation(String reservationCode);

    List<String> findFlightNumbersByReservation(String reservationCode);
}
