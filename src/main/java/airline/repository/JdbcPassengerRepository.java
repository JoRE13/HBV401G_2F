package airline.repository;

import airline.db.ConnectionFactory;
import airline.model.Passenger;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcPassengerRepository implements PassengerRepository {
    private final ConnectionFactory connectionFactory;

    public JdbcPassengerRepository() {
        this(ConnectionFactory.fromEnvironment());
    }

    public JdbcPassengerRepository(ConnectionFactory connectionFactory) {
        if (connectionFactory == null) {
            throw new IllegalArgumentException("connectionFactory cannot be null");
        }
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void save(Passenger passenger) {
        // Upsert a passenger med email sem natural key.
        String sql = """
                INSERT INTO passengers (full_name, email, phone, nationality, date_of_birth)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (email)
                DO UPDATE SET full_name = EXCLUDED.full_name,
                              phone = EXCLUDED.phone,
                              nationality = EXCLUDED.nationality,
                              date_of_birth = EXCLUDED.date_of_birth
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passenger.getFullName());
            statement.setString(2, passenger.getEmail());
            statement.setString(3, passenger.getPhone());
            statement.setString(4, passenger.getNationality());
            statement.setDate(5, new Date(passenger.getDateOfBirth().getTime()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw dataAccess("save", e);
        }
    }

    @Override
    public Passenger findByEmail(String email) {
        // Email er PK, thannig leit er einfaldlega 0 eða 1 nidurstada.
        String sql = """
                SELECT full_name, email, phone, nationality, date_of_birth
                FROM passengers
                WHERE email = ?
                """;
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return new Passenger(
                        resultSet.getString("full_name"),
                        resultSet.getString("email"),
                        resultSet.getString("phone"),
                        resultSet.getString("nationality"),
                        new java.util.Date(resultSet.getDate("date_of_birth").getTime())
                );
            }
        } catch (SQLException e) {
            throw dataAccess("findByEmail", e);
        }
    }

    private RuntimeException dataAccess(String operation, SQLException e) {
        return new IllegalStateException("Database operation failed: " + operation, e);
    }
}

