package airline;

import airline.controllers.FlightController;
import airline.controllers.ReservationController;
import airline.db.ConnectionFactory;
import airline.repository.AirportRepository;
import airline.repository.FlightRepository;
import airline.repository.JdbcAirportRepository;
import airline.repository.JdbcFlightRepository;
import airline.repository.JdbcPassengerRepository;
import airline.repository.JdbcReservationRepository;
import airline.repository.PassengerRepository;
import airline.repository.ReservationRepository;

/**
 * Production bootstrap for the Flight Search component.
 *
 * This class wires JDBC repositories and controllers in one place so
 * external consumers (for Team T) can obtain a ready-to-use component.
 */
public final class Application {
    private Application() {
    }

    /**
     * Container for fully wired production dependencies.
     */
    public static final class Components {
        private final FlightController flightController;
        private final ReservationController reservationController;
        private final AirportRepository airportRepository;
        private final FlightRepository flightRepository;
        private final PassengerRepository passengerRepository;
        private final ReservationRepository reservationRepository;

        public Components(
                FlightController flightController,
                ReservationController reservationController,
                AirportRepository airportRepository,
                FlightRepository flightRepository,
                PassengerRepository passengerRepository,
                ReservationRepository reservationRepository) {
            this.flightController = flightController;
            this.reservationController = reservationController;
            this.airportRepository = airportRepository;
            this.flightRepository = flightRepository;
            this.passengerRepository = passengerRepository;
            this.reservationRepository = reservationRepository;
        }

        public FlightController getFlightController() {
            return flightController;
        }

        public ReservationController getReservationController() {
            return reservationController;
        }

        public AirportRepository getAirportRepository() {
            return airportRepository;
        }

        public FlightRepository getFlightRepository() {
            return flightRepository;
        }

        public PassengerRepository getPassengerRepository() {
            return passengerRepository;
        }

        public ReservationRepository getReservationRepository() {
            return reservationRepository;
        }
    }

    /**
     * Builds production repositories/controllers backed by PostgreSQL.
     *
     * Connection settings are read from environment variables in ConnectionFactory.
     */
    public static Components createProductionComponents() {
        ConnectionFactory connectionFactory = ConnectionFactory.fromEnvironment();

        AirportRepository airportRepository = new JdbcAirportRepository(connectionFactory);
        FlightRepository flightRepository = new JdbcFlightRepository(connectionFactory, airportRepository);
        PassengerRepository passengerRepository = new JdbcPassengerRepository(connectionFactory);
        ReservationRepository reservationRepository = new JdbcReservationRepository(connectionFactory);

        FlightController flightController = new FlightController(flightRepository);
        ReservationController reservationController = new ReservationController(
                reservationRepository,
                passengerRepository,
                flightRepository
        );

        return new Components(
                flightController,
                reservationController,
                airportRepository,
                flightRepository,
                passengerRepository,
                reservationRepository
        );
    }

    /**
     * Minimal startup check for demo/presentation usage.
     */
    public static void main(String[] args) {
        Components components = createProductionComponents();
        int flightsInStorage = components.getFlightRepository().findAll().size();
        System.out.println("Flight component ready. Flights in storage: " + flightsInStorage);
    }
}
