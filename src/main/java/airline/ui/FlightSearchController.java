package airline.ui;

import airline.controllers.FlightController;
import airline.model.Flight;
import airline.model.Itinerary;
import airline.repository.JdbcFlightRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class FlightSearchController {

    private final FlightController flightController =
            new FlightController(new JdbcFlightRepository());

    @FXML
    private TextField fromTextField;

    @FXML
    private TextField toTextField;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private CheckBox connectingFlightsCheckBox;

    @FXML
    private ListView<String> resultsListView;

    @FXML
    public void initialize() {
        sortComboBox.setItems(FXCollections.observableArrayList(
                "Price",
                "Duration",
                "Departure time",
                "Arrival time",
                "Available seats"
        ));

        sortComboBox.setValue("Price");

        fromDatePicker.setValue(LocalDate.now());
        toDatePicker.setValue(LocalDate.now().plusDays(7));

        resultsListView.setItems(FXCollections.observableArrayList());
    }

    @FXML
    private void handleSearch() {
        String from = fromTextField.getText().trim().toUpperCase();
        String to = toTextField.getText().trim().toUpperCase();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        String sortBy = sortComboBox.getValue();
        boolean allowConnectingFlights = connectingFlightsCheckBox.isSelected();

        if (from.isEmpty() || to.isEmpty()) {
            resultsListView.setItems(FXCollections.observableArrayList(
                    "Please enter both airport codes."
            ));
            return;
        }

        if (from.length() != 3 || to.length() != 3) {
            resultsListView.setItems(FXCollections.observableArrayList(
                    "Airport codes must be 3 letters."
            ));
            return;
        }

        if (from.equals(to)) {
            resultsListView.setItems(FXCollections.observableArrayList(
                    "Departure and arrival airport cannot be the same."
            ));
            return;
        }

        if (fromDate == null || toDate == null) {
            resultsListView.setItems(FXCollections.observableArrayList(
                    "Please select both dates."
            ));
            return;
        }

        if (toDate.isBefore(fromDate)) {
            resultsListView.setItems(FXCollections.observableArrayList(
                    "\"Date to\" can not be before \"date from\"."
            ));
            return;
        }

        searchFlights(from, to, fromDate, toDate, sortBy, allowConnectingFlights);
    }
    private void searchFlights(String from, String to,
                LocalDate fromDate, LocalDate toDate,
                String sortBy, boolean allowConnectingFlights) {

        try {
            ZoneId zone = ZoneId.of("UTC");

            ZonedDateTime startDateTime = fromDate.atStartOfDay(zone);
            ZonedDateTime endDateTime = toDate.plusDays(1).atStartOfDay(zone);

            List<Flight> flights = flightController.searchFlights(from, to, startDateTime);

            flights = flightController.filterByDepartureTimeRange(flights, startDateTime, endDateTime);

            if ("Price".equals(sortBy)) {
                flights = flightController.sortByPrice(flights);
            } else if ("Duration".equals(sortBy)) {
                flights.sort((a, b) -> Integer.compare(a.getDurationMinutes(), b.getDurationMinutes()));
            } else if ("Departure time".equals(sortBy)) {
                flights.sort((a, b) -> a.getDepartureDateTime().compareTo(b.getDepartureDateTime()));
            } else if ("Arrival time".equals(sortBy)) {
                flights.sort((a, b) -> a.getArrivalDateTime().compareTo(b.getArrivalDateTime()));
            } else if ("Available seats".equals(sortBy)) {
                flights.sort((a, b) -> Integer.compare(b.getAvailableSeatCount(), a.getAvailableSeatCount()));
            }

            List<String> results = new ArrayList<>();

            for (Flight flight : flights) {
                results.add(formatFlight(flight));
            }

            if (allowConnectingFlights) {
                List<Itinerary> itineraries =
                        flightController.findConnectingItineraries(from, to, startDateTime);

                for (Itinerary itinerary : itineraries) {
                    results.add(formatItinerary(itinerary));
                }
            }

            displayFlights(results);

        } catch (Exception e) {
            e.printStackTrace();

            String details = e.getMessage();
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                details = e.getCause().getMessage();
            }

            resultsListView.setItems(FXCollections.observableArrayList(
                    "Error while searching flights.",
                    details
            ));
        }
    }

    private void displayFlights(List<String> flights) {
        if (flights == null || flights.isEmpty()) {
            resultsListView.setItems(FXCollections.observableArrayList(
                    "No flights found."
            ));
            return;
        }

        resultsListView.setItems(FXCollections.observableArrayList(flights));
    }

    private String formatFlight(Flight flight) {
        return flight.getDepartureAirport().getAirportCode() + " -> "
                + flight.getArrivalAirport().getAirportCode()
                + " | Flight: " + flight.getFlightNumber()
                + " | Departure: " + flight.getDepartureDateTime().toLocalDateTime()
                + " | Arrival: " + flight.getArrivalDateTime().toLocalDateTime()
                + " | Duration: " + flight.getDurationMinutes() + " min"
                + " | Price: " + flight.getBasePrice()
                + " | Seats: " + flight.getAvailableSeatCount()
                + " | Status: " + flight.getStatus();
    }

    private String formatItinerary(Itinerary itinerary) {
        List<Flight> legs = itinerary.getLegs();

        if (legs == null || legs.isEmpty()) {
            return "Connecting flight"
                    + " | Itinerary: " + itinerary.getItineraryId()
                    + " | Duration: " + itinerary.getTotalDuration() + " min"
                    + " | Price: " + itinerary.getTotalPrice();
        }

        String from = legs.get(0).getDepartureAirport().getAirportCode();
        String to = legs.get(legs.size() - 1).getArrivalAirport().getAirportCode();

        return "Connecting flight"
                + " | Route: " + from + " -> " + to
                + " | Itinerary: " + itinerary.getItineraryId()
                + " | Duration: " + itinerary.getTotalDuration() + " min"
                + " | Price: " + itinerary.getTotalPrice();
    }
}
