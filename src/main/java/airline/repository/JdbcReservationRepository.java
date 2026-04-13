package airline.repository;

import airline.db.ConnectionFactory;
import airline.model.Reservation;
import airline.model.ReservationItem;
import airline.model.ReservationStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JdbcReservationRepository implements ReservationRepository {
    private final ConnectionFactory connectionFactory;

    public JdbcReservationRepository() {
        this(ConnectionFactory.fromEnvironment());
    }

    public JdbcReservationRepository(ConnectionFactory connectionFactory) {
        if (connectionFactory == null) {
            throw new IllegalArgumentException("connectionFactory cannot be null");
        }
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void save(Reservation reservation) {
        String sql = """
                INSERT INTO reservations (reservation_code, created_at, status, total_price)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindReservation(statement, reservation, false);
            statement.executeUpdate();
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Reservation already exists: " + reservation.getReservationCode());
            }
            throw dataAccess("save", e);
        }
    }

    @Override
    public void update(Reservation reservation) {
        String sql = """
                UPDATE reservations
                SET created_at = ?,
                    status = ?,
                    total_price = ?
                WHERE reservation_code = ?
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindReservation(statement, reservation, true);
            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("Reservation not found: " + reservation.getReservationCode());
            }
        } catch (SQLException e) {
            throw dataAccess("update", e);
        }
    }

    @Override
    public Reservation findByCode(String reservationCode) {
        String sql = """
                SELECT reservation_code, created_at, status, total_price
                FROM reservations
                WHERE reservation_code = ?
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reservationCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapReservation(resultSet);
            }
        } catch (SQLException e) {
            throw dataAccess("findByCode", e);
        }
    }

    @Override
    public List<Reservation> findByFlight(String flightNumber) {
        String sql = """
                SELECT r.reservation_code, r.created_at, r.status, r.total_price
                FROM reservations r
                JOIN reservation_flights rf ON rf.reservation_code = r.reservation_code
                WHERE rf.flight_number = ?
                ORDER BY r.created_at
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, flightNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Reservation> reservations = new ArrayList<>();
                while (resultSet.next()) {
                    reservations.add(mapReservation(resultSet));
                }
                return reservations;
            }
        } catch (SQLException e) {
            throw dataAccess("findByFlight", e);
        }
    }

    @Override
    public void linkFlight(String reservationCode, String flightNumber) {
        String sql = """
                INSERT INTO reservation_flights (reservation_code, flight_number)
                VALUES (?, ?)
                ON CONFLICT (reservation_code, flight_number) DO NOTHING
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reservationCode);
            statement.setString(2, flightNumber);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw dataAccess("linkFlight", e);
        }
    }

    @Override
    public void saveItem(String reservationCode, ReservationItem item) {
        String sql = """
                INSERT INTO reservation_items (item_id, reservation_code, passenger_email, price_paid)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, item.getItemId());
            statement.setString(2, reservationCode);
            statement.setString(3, item.getPassengerEmail());
            statement.setDouble(4, item.getPricePaid());
            statement.executeUpdate();
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Reservation item already exists: " + item.getItemId());
            }
            throw dataAccess("saveItem", e);
        }
    }

    @Override
    public void assignSeat(String reservationCode, String itemId, String flightNumber, String seatId) {
        String sql = """
                INSERT INTO reservation_item_flights (item_id, flight_number, seat_id)
                SELECT ri.item_id, rf.flight_number, ?
                FROM reservation_items ri
                JOIN reservation_flights rf
                  ON rf.reservation_code = ri.reservation_code
                WHERE ri.item_id = ?
                  AND ri.reservation_code = ?
                  AND rf.flight_number = ?
                ON CONFLICT (item_id, flight_number)
                DO UPDATE SET seat_id = EXCLUDED.seat_id
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, seatId);
            statement.setString(2, itemId);
            statement.setString(3, reservationCode);
            statement.setString(4, flightNumber);
            int changed = statement.executeUpdate();
            if (changed == 0) {
                throw new IllegalArgumentException(
                        "Reservation item or flight leg not found for reservation: "
                                + reservationCode + ", item: " + itemId + ", flight: " + flightNumber
                );
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException(
                        "Seat already assigned for flight " + flightNumber + ": " + seatId
                );
            }
            throw dataAccess("assignSeat", e);
        }
    }

    @Override
    public List<ReservationItem> findItemsByReservation(String reservationCode) {
        String sql = """
                SELECT item_id, reservation_code, passenger_email, price_paid
                FROM reservation_items
                WHERE reservation_code = ?
                ORDER BY item_id
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reservationCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<ReservationItem> items = new ArrayList<>();
                while (resultSet.next()) {
                    items.add(new ReservationItem(
                            resultSet.getString("item_id"),
                            resultSet.getDouble("price_paid"),
                            resultSet.getString("passenger_email")
                    ));
                }
                return items;
            }
        } catch (SQLException e) {
            throw dataAccess("findItemsByReservation", e);
        }
    }

    @Override
    public List<String> findFlightNumbersByReservation(String reservationCode) {
        String sql = """
                SELECT flight_number
                FROM reservation_flights
                WHERE reservation_code = ?
                ORDER BY flight_number
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reservationCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<String> flightNumbers = new ArrayList<>();
                while (resultSet.next()) {
                    flightNumbers.add(resultSet.getString("flight_number"));
                }
                return flightNumbers;
            }
        } catch (SQLException e) {
            throw dataAccess("findFlightNumbersByReservation", e);
        }
    }

    @Override
    public boolean hasSeatAssignment(String reservationCode, String itemId, String flightNumber) {
        String sql = """
                SELECT 1
                FROM reservation_item_flights rif
                JOIN reservation_items ri
                  ON ri.item_id = rif.item_id
                WHERE ri.reservation_code = ?
                  AND rif.item_id = ?
                  AND rif.flight_number = ?
                LIMIT 1
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reservationCode);
            statement.setString(2, itemId);
            statement.setString(3, flightNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw dataAccess("hasSeatAssignment", e);
        }
    }

    private void bindReservation(PreparedStatement statement, Reservation reservation, boolean forUpdate)
            throws SQLException {
        Timestamp createdAt = new Timestamp(reservation.getCreatedAt().getTime());
        if (!forUpdate) {
            statement.setString(1, reservation.getReservationCode());
            statement.setTimestamp(2, createdAt);
            statement.setString(3, reservation.getStatus().name());
            statement.setDouble(4, reservation.getTotalPrice());
        } else {
            statement.setTimestamp(1, createdAt);
            statement.setString(2, reservation.getStatus().name());
            statement.setDouble(3, reservation.getTotalPrice());
            statement.setString(4, reservation.getReservationCode());
        }
    }

    private Reservation mapReservation(ResultSet resultSet) throws SQLException {
        ReservationStatus status = ReservationStatus.valueOf(resultSet.getString("status"));
        return new Reservation(
                resultSet.getString("reservation_code"),
                new Date(resultSet.getTimestamp("created_at").getTime()),
                status,
                resultSet.getDouble("total_price")
        );
    }

    private RuntimeException dataAccess(String operation, SQLException e) {
        return new IllegalStateException("Database operation failed: " + operation, e);
    }
}
