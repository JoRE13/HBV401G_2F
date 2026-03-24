package src.main.java.airline.controllers;

import src.main.java.airline.model.Flight;
import src.main.java.airline.model.Itinerary;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FlightController {
    private List<Flight> flights;

    public FlightController(){
        flights = new ArrayList<>();
    }

    //get departing and arriving flights

    public List<Flight> getDepartingFlights(String airportCode){
        List<Flight> departing = new ArrayList<>();
        for (Flight f : flights) {
            if (f.getDepartureAirport().getAirportCode().equals(airportCode)){
                departing.add(f);
            }
        }
        return departing;
    }

    public List <Flight> getArrivingFlight(String airportCode){
        List<Flight> arriving = new ArrayList<>();
        for(Flight f : flights) {
            if(f.getArrivalAirport().getAirportCode().equals(airportCode)){
                arriving.add(f);}
        }
        return arriving;
    }

    //search methods
    public List<Flight> searchFlights(
            String departureCode,
            String arrivalCode,
            ZonedDateTime date){
        List<Flight> search = new ArrayList<>();
        for (Flight f : flights) {
            if (f.getDepartureAirport().getAirportCode().equals(departureCode) &&
                    f.getArrivalAirport().getAirportCode().equals(arrivalCode) &&
                    f.getDepartureDateTime().equals(date)) {
                search.add(f);
            }
        }
        return search;
    }

    public List<Flight> searchByDepartureAirport(
            String airportCode,
            ZonedDateTime date){
        List<Flight> departureSearch = new ArrayList<>();
        for(Flight f : flights) {
            if(f.getDepartureAirport().getAirportCode().equals(airportCode) &&
            f.getDepartureDateTime().equals(date)){
                departureSearch.add(f);
            }
        }
        return departureSearch;
    }

    public List<Flight> filterByDepartureTimeRange(
            List<Flight> flights,
            ZonedDateTime start,
            ZonedDateTime end){
        List<Flight> filterResult = new ArrayList<>();
        for (Flight f : flights) {
            ZonedDateTime departure = f.getDepartureDateTime();
            if (departure.isAfter(start) && departure.isBefore(end)){
                filterResult.add(f);
            }
        }
        return filterResult;
    }

    public List<Flight> sortByPrice(List<Flight> flights){
        flights.sort(Comparator.comparingDouble(Flight::getBasePrice));
    return flights;
    }

    public List<Itinerary> findConnectingItineraries(
            String fromCode,
            String toCode
            ZonedDateTime date){
        List<Itinerary> result = new ArrayList<>();


    }

    public static void main(String[] args) {

    }
}
