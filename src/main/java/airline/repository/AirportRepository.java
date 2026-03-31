package airline.repository;

import airline.model.Airport;

import java.util.List;

public interface AirportRepository {
    void save(Airport airport);

    Airport findByCode(String code);

    List<Airport> findAll();
}
