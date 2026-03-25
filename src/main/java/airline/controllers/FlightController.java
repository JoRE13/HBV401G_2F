package airline.controllers;

import airline.model.Flight;
import airline.model.Itinerary;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FlightController {
    private List<Flight> flights;

    public FlightController() {
        flights = new ArrayList<>();
    }

    // get departing and arriving flights

    public List<Flight> getDepartingFlights(String airportCode) {
        List<Flight> departing = new ArrayList<>();
        for (Flight f : flights) {
            if (f.getDepartureAirport().getAirportCode().equals(airportCode)) {
                departing.add(f);
            }
        }
        return departing;
    }

    public List<Flight> getArrivingFlights(String airportCode) {
        List<Flight> arriving = new ArrayList<>();
        for (Flight f : flights) {
            if (f.getArrivalAirport().getAirportCode().equals(airportCode)) {
                arriving.add(f);
            }
        }
        return arriving;
    }

    // search methods
    public List<Flight> searchFlights(
            String departureCode,
            String arrivalCode,
            ZonedDateTime date) {
        List<Flight> search = new ArrayList<>();
        for (Flight f : flights) {
            if (f.getDepartureAirport().getAirportCode().equals(departureCode) &&
                    f.getArrivalAirport().getAirportCode().equals(arrivalCode) &&
                    f.getDepartureDateTime().toLocalDate().equals(date.toLocalDate())) {
                search.add(f);
            }
        }
        return search;
    }

    public List<Flight> searchByDepartureAirport(
            String airportCode,
            ZonedDateTime date) {
        List<Flight> departureSearch = new ArrayList<>();
        for (Flight f : flights) {
            if (f.getDepartureAirport().getAirportCode().equals(airportCode) &&
                    f.getDepartureDateTime().toLocalDate().equals(date.toLocalDate())) {
                departureSearch.add(f);
            }
        }
        return departureSearch;
    }

    public List<Flight> filterByDepartureTimeRange(
            List<Flight> inputFlights,
            ZonedDateTime start,
            ZonedDateTime end) {
        List<Flight> filterResult = new ArrayList<>();
        for (Flight f : flights) {
            ZonedDateTime departure = f.getDepartureDateTime();
            if (departure.isAfter(start) && departure.isBefore(end)) {
                filterResult.add(f);
            }
        }
        return filterResult;
    }

    public List<Flight> sortByPrice(List<Flight> flights) {
        flights.sort(Comparator.comparingDouble(Flight::getBasePrice));
        return flights;
    }

    public List<Itinerary> findConnectingItineraries(
            String fromCode,
            String toCode,
            ZonedDateTime date) {
        List<Itinerary> result = new ArrayList<>();

        for (Flight f1 : flights) {
            if (!f1.getDepartureAirport().getAirportCode().equals(fromCode))
                continue;

            for (Flight f2 : flights) {
                if (f1.getArrivalAirport().getAirportCode()
                        .equals(f2.getDepartureAirport().getAirportCode())
                        && f2.getArrivalAirport().getAirportCode().equals(toCode)) {

                    List<Flight> legs = new ArrayList<>();
                    legs.add(f1);
                    legs.add(f2);

                    Itinerary itin = new Itinerary(
                            (f1.getFlightNumber() + " - " + f2.getFlightNumber()),
                            0,
                            0,
                            legs);

                    itin.computeTotalDuration();
                    itin.computeTotalPrice();

                    result.add(itin);
                }
            }
        }
        return result;
    }

    // Flug-valkostir
    public void addFlight(Flight flight) {
        flights.add(flight);
    }

    public void updateFlight(Flight flight) {
        removeFlight(flight.getFlightNumber());
        flights.add(flight);
    }

    public void removeFlight(String flightNumber) {
        flights.removeIf(f -> f.getFlightNumber().equals(flightNumber));
    }

    public void cancelFlight(String flightNumber) {
        for (Flight f : flights) {
            if (f.getFlightNumber().equals(flightNumber)) {
                f.setStatusCancelled();
            }
        }
    }

    public void rescheduleFlight(
            String flightNumber,
            ZonedDateTime newDepartureTime,
            ZonedDateTime newArrivalTime) {
        for (Flight f : flights) {
            if (f.getFlightNumber().equals(flightNumber)) {
                f.reschedule(newDepartureTime, newArrivalTime);
            }
        }
    }

    public static void main(String[] args) {

    }

}
