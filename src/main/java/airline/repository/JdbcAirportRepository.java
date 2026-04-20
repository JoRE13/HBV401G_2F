package airline.repository;

import airline.db.ConnectionFactory;
import airline.model.Airport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcAirportRepository implements AirportRepository {
    private final ConnectionFactory connectionFactory;

    public JdbcAirportRepository() {
        this(ConnectionFactory.fromEnvironment());
    }

    public JdbcAirportRepository(ConnectionFactory connectionFactory) {
        if (connectionFactory == null) {
            throw new IllegalArgumentException("connectionFactory cannot be null");
        }
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void save(Airport airport) {
        // Upsert: setur inn nyjan airport eða uppfaerir ef kodi er til.
        String sql = """
                INSERT INTO airports (airport_code, name, city, country)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (airport_code)
                DO UPDATE SET name = EXCLUDED.name,
                              city = EXCLUDED.city,
                              country = EXCLUDED.country
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, airport.getAirportCode());
            statement.setString(2, airport.getName());
            statement.setString(3, airport.getCity());
            statement.setString(4, airport.getCountry());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw dataAccess("save", e);
        }
    }

    @Override
    public Airport findByCode(String code) {
        // Skilar null ef airport finnst ekki.
        String sql = "SELECT airport_code, name, city, country FROM airports WHERE airport_code = ?";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, code);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapAirport(resultSet);
            }
        } catch (SQLException e) {
            throw dataAccess("findByCode", e);
        }
    }

    @Override
    public List<Airport> findAll() {
        String sql = "SELECT airport_code, name, city, country FROM airports ORDER BY airport_code";
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Airport> airports = new ArrayList<>();
            while (resultSet.next()) {
                airports.add(mapAirport(resultSet));
            }
            return airports;
        } catch (SQLException e) {
            throw dataAccess("findAll", e);
        }
    }

    private Airport mapAirport(ResultSet resultSet) throws SQLException {
        // Breytir DB row i domain object.
        return new Airport(
                resultSet.getString("airport_code"),
                resultSet.getString("name"),
                resultSet.getString("city"),
                resultSet.getString("country")
        );
    }

    private RuntimeException dataAccess(String operation, SQLException e) {
        return new IllegalStateException("Database operation failed: " + operation, e);
    }
}
