package airline.repository;

import airline.model.Flight;

import java.util.List;

public interface FlightRepository {

    List<Flight> findAll();
}
