package airline.repository;

import airline.db.ConnectionFactory;
import airline.model.Airport;
import airline.model.Flight;
import airline.model.FlightStatus;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL-backed implementation of FlightRepository using JDBC.
 */
public class JdbcFlightRepository implements FlightRepository {
    // Grunnselect sem joinar airports svo mapFlight hafi allt i einu resultsetti.
    private static final String SELECT_BASE = """
            SELECT
                f.flight_number,
                f.departure_at,
                f.arrival_at,
                f.duration_minutes,
                f.base_price,
                f.status,
                f.capacity,
                dep.airport_code AS dep_airport_code,
                dep.name AS dep_name,
                dep.city AS dep_city,
                dep.country AS dep_country,
                arr.airport_code AS arr_airport_code,
                arr.name AS arr_name,
                arr.city AS arr_city,
                arr.country AS arr_country,
                f.airplane_type
            FROM flights f
            JOIN airports dep ON dep.airport_code = f.departure_airport_code
            JOIN airports arr ON arr.airport_code = f.arrival_airport_code
            """;

    private final ConnectionFactory connectionFactory;
    private final AirportRepository airportRepository;

    public JdbcFlightRepository() {
        this(ConnectionFactory.fromEnvironment());
    }

    public JdbcFlightRepository(ConnectionFactory connectionFactory) {
        this(connectionFactory, new JdbcAirportRepository(connectionFactory));
    }

    public JdbcFlightRepository(ConnectionFactory connectionFactory, AirportRepository airportRepository) {
        if (connectionFactory == null) {
            throw new IllegalArgumentException("connectionFactory cannot be null");
        }
        if (airportRepository == null) {
            throw new IllegalArgumentException("airportRepository cannot be null");
        }
        this.connectionFactory = connectionFactory;
        this.airportRepository = airportRepository;
    }

    @Override
    public List<Flight> findAll() {
        String sql = SELECT_BASE + " ORDER BY f.departure_at";
        try (Connection connection = connectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            return mapFlights(resultSet);
        } catch (SQLException e) {
            throw dataAccess("findAll", e);
        }
    }

    @Override
    public Flight findByFlightNumber(String flightNumber) {
        String sql = SELECT_BASE + " WHERE f.flight_number = ?";
        try (Connection connection = connectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, flightNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapFlight(resultSet);
                }
                return null;
            }
        } catch (SQLException e) {
            throw dataAccess("findByFlightNumber", e);
        }
    }

    @Override
    public List<Flight> findByRouteAndDate(String departureCode, String arrivalCode, ZonedDateTime date) {
        String sql = SELECT_BASE + """
                 WHERE f.departure_airport_code = ?
                   AND f.arrival_airport_code = ?
                   AND (f.departure_at AT TIME ZONE 'UTC')::date = ?
                 ORDER BY f.departure_at
                """;
        try (Connection connection = connectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, departureCode);
            statement.setString(2, arrivalCode);
            statement.setDate(3, Date.valueOf(date.toLocalDate()));
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapFlights(resultSet);
            }
        } catch (SQLException e) {
            throw dataAccess("findByRouteAndDate", e);
        }
    }

    @Override
    public List<Flight> findByRouteAndDateRange(String departureCode, String arrivalCode, ZonedDateTime start,
            ZonedDateTime end) {
        String sql = SELECT_BASE + """
                 WHERE f.departure_airport_code = ?
                   AND f.arrival_airport_code = ?
                   AND (f.departure_at AT TIME ZONE 'UTC')::date >= ?
                   AND (f.departure_at AT TIME ZONE 'UTC')::date < ?
                 ORDER BY f.departure_at
                """;
        try (Connection connection = connectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, departureCode);
            statement.setString(2, arrivalCode);
            statement.setDate(3, Date.valueOf(start.toLocalDate()));
            statement.setDate(4, Date.valueOf(end.toLocalDate()));
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapFlights(resultSet);
            }
        } catch (SQLException e) {
            throw dataAccess("findByRouteAndDate", e);
        }
    }

    @Override
    public List<Flight> findByDepartureAirportAndDate(String airportCode, ZonedDateTime date) {
        String sql = SELECT_BASE + """
                 WHERE f.departure_airport_code = ?
                   AND (f.departure_at AT TIME ZONE 'UTC')::date = ?
                 ORDER BY f.departure_at
                """;
        try (Connection connection = connectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, airportCode);
            statement.setDate(2, Date.valueOf(date.toLocalDate()));
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapFlights(resultSet);
            }
        } catch (SQLException e) {
            throw dataAccess("findByDepartureAirportAndDate", e);
        }
    }

    @Override
    public List<Flight> findDepartingFlights(String airportCode) {
        String sql = SELECT_BASE + """
                 WHERE f.departure_airport_code = ?
                 ORDER BY f.departure_at
                """;
        try (Connection connection = connectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, airportCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapFlights(resultSet);
            }
        } catch (SQLException e) {
            throw dataAccess("findDepartingFlights", e);
        }
    }

    @Override
    public List<Flight> findArrivingFlights(String airportCode) {
        String sql = SELECT_BASE + """
                 WHERE f.arrival_airport_code = ?
                 ORDER BY f.arrival_at
                """;
        try (Connection connection = connectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, airportCode);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapFlights(resultSet);
            }
        } catch (SQLException e) {
            throw dataAccess("findArrivingFlights", e);
        }
    }

    @Override
    public int findAvailableSeatCount(String flightNumber) {
        // Availability reiknast ur DB: capacity - fjoldi seat assignments.
        String sql = """
                SELECT GREATEST(f.capacity - COALESCE(booked.booked_count, 0), 0) AS available_seats
                FROM flights f
                LEFT JOIN (
                    SELECT flight_number, COUNT(*) AS booked_count
                    FROM reservation_item_flights
                    GROUP BY flight_number
                ) booked ON booked.flight_number = f.flight_number
                WHERE f.flight_number = ?
                """;
        try (Connection connection = connectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, flightNumber);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Flight not found: " + flightNumber);
                }
                return resultSet.getInt("available_seats");
            }
        } catch (SQLException e) {
            throw dataAccess("findAvailableSeatCount", e);
        }
    }

    @Override
    public void save(Flight flight) {
        // Tryggjum ad airports seu til adur en flight row er inserted.
        airportRepository.save(flight.getDepartureAirport());
        airportRepository.save(flight.getArrivalAirport());

        String sql = """
                INSERT INTO flights (
                    flight_number,
                    departure_at,
                    arrival_at,
                    duration_minutes,
                    base_price,
                    status,
                    capacity,
                    departure_airport_code,
                    arrival_airport_code,
                    airplane_type
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            bindFlight(statement, flight);
            statement.executeUpdate();
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Flight already exists: " + flight.getFlightNumber());
            }
            throw dataAccess("save", e);
        }
    }

    @Override
    public void update(Flight flight) {
        // Sama regla i update: airports eru upsertud fyrst.
        airportRepository.save(flight.getDepartureAirport());
        airportRepository.save(flight.getArrivalAirport());

        String sql = """
                UPDATE flights
                SET departure_at = ?,
                    arrival_at = ?,
                    duration_minutes = ?,
                    base_price = ?,
                    status = ?,
                    capacity = ?,
                    departure_airport_code = ?,
                    arrival_airport_code = ?,
                    airplane_type = ?
                WHERE flight_number = ?
                """;
        try (Connection connection = connectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.from(flight.getDepartureDateTime().toInstant()));
            statement.setTimestamp(2, Timestamp.from(flight.getArrivalDateTime().toInstant()));
            statement.setInt(3, flight.getDurationMinutes());
            statement.setDouble(4, flight.getBasePrice());
            statement.setString(5, flight.getStatus().name());
            statement.setInt(6, flight.getCapacity());
            statement.setString(7, flight.getDepartureAirport().getAirportCode());
            statement.setString(8, flight.getArrivalAirport().getAirportCode());
            statement.setString(9, flight.getAirplaneType());
            statement.setString(10, flight.getFlightNumber());

            int updated = statement.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("Flight not found: " + flight.getFlightNumber());
            }
        } catch (SQLException e) {
            throw dataAccess("update", e);
        }
    }

    @Override
    public void delete(String flightNumber) {
        String sql = "DELETE FROM flights WHERE flight_number = ?";
        try (Connection connection = connectionFactory.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, flightNumber);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw dataAccess("delete", e);
        }
    }

    private void bindFlight(PreparedStatement statement, Flight flight) throws SQLException {
        // Placeholder rod i SQL insert: 1..10.
        statement.setString(1, flight.getFlightNumber());
        statement.setTimestamp(2, Timestamp.from(flight.getDepartureDateTime().toInstant()));
        statement.setTimestamp(3, Timestamp.from(flight.getArrivalDateTime().toInstant()));
        statement.setInt(4, flight.getDurationMinutes());
        statement.setDouble(5, flight.getBasePrice());
        statement.setString(6, flight.getStatus().name());
        statement.setInt(7, flight.getCapacity());
        statement.setString(8, flight.getDepartureAirport().getAirportCode());
        statement.setString(9, flight.getArrivalAirport().getAirportCode());
        statement.setString(10, flight.getAirplaneType());
    }

    private List<Flight> mapFlights(ResultSet resultSet) throws SQLException {
        List<Flight> flights = new ArrayList<>();
        while (resultSet.next()) {
            flights.add(mapFlight(resultSet));
        }
        return flights;
    }

    private Flight mapFlight(ResultSet resultSet) throws SQLException {
        // DB timestamp er normaliserad i UTC fyrir samraemda domain hegdun.
        int capacity = resultSet.getInt("capacity");
        Airport departureAirport = new Airport(
                resultSet.getString("dep_airport_code"),
                resultSet.getString("dep_name"),
                resultSet.getString("dep_city"),
                resultSet.getString("dep_country"));
        Airport arrivalAirport = new Airport(
                resultSet.getString("arr_airport_code"),
                resultSet.getString("arr_name"),
                resultSet.getString("arr_city"),
                resultSet.getString("arr_country"));
        ZonedDateTime departure = resultSet.getTimestamp("departure_at").toInstant().atZone(ZoneId.of("UTC"));
        ZonedDateTime arrival = resultSet.getTimestamp("arrival_at").toInstant().atZone(ZoneId.of("UTC"));
        FlightStatus status = FlightStatus.valueOf(resultSet.getString("status"));
        double basePrice = resultSet.getDouble("base_price");

        return new Flight(
                resultSet.getString("flight_number"),
                departure,
                arrival,
                basePrice,
                status,
                capacity,
                arrivalAirport,
                departureAirport,
                resultSet.getString("airplane_type"));
    }

    private RuntimeException dataAccess(String operation, SQLException e) {
        return new IllegalStateException("Database operation failed: " + operation, e);
    }
}
