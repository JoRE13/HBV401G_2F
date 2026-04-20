package airline.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Creates JDBC connections for the application's repositories.
 *
 * Environment variables:
 * AIRLINE_DB_URL, AIRLINE_DB_USER, AIRLINE_DB_PASSWORD.
 */
public class ConnectionFactory {
    private final String url;
    private final String user;
    private final String password;

    public ConnectionFactory(String url, String user, String password) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url cannot be null or blank");
        }
        if (user == null || user.isBlank()) {
            throw new IllegalArgumentException("user cannot be null or blank");
        }
        this.url = url;
        this.user = user;
        this.password = password == null ? "" : password;
    }

    public static ConnectionFactory fromEnvironment() {
        // Sja default values fyrir local keyrslu ef env vars vantar.
        return new ConnectionFactory(
                env("AIRLINE_DB_URL", "jdbc:postgresql://localhost:5432/airline"),
                env("AIRLINE_DB_USER", "postgres"),
                env("AIRLINE_DB_PASSWORD", "postgres")
        );
    }

    public Connection getConnection() throws SQLException {
        // Ollum JDBC repos er veitt connection i gegnum sama factory.
        return DriverManager.getConnection(url, user, password);
    }

    private static String env(String key, String fallback) {
        // Tekur env value ef til er, annars fallback.
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? fallback : value;
    }
}

