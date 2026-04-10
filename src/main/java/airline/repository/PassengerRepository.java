package airline.repository;

import airline.model.Passenger;

public interface PassengerRepository {
    void save(Passenger passenger);

    Passenger findByEmail(String email);
}

