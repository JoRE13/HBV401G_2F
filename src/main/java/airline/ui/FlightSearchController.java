package airline.ui;

import airline.Application;
import airline.controllers.FlightController;
import airline.controllers.ReservationController;
import airline.model.Airport;
import airline.model.Flight;
import airline.model.Itinerary;
import airline.model.Passenger;
import airline.model.Reservation;
import airline.model.ReservationItem;
import airline.repository.AirportRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FlightSearchController {

    // UI notar production wiring beint i þessari demo utgafu.
    private final Application.Components components = Application.createProductionComponents();
    private final FlightController flightController = components.getFlightController();
    private final ReservationController reservationController = components.getReservationController();
    private final AirportRepository airportRepository = components.getAirportRepository();
    // Key: textalinan sem birttist i listanum, Value: underlying direct flight.
    private final Map<String, Flight> directFlightsByResultLine = new HashMap<>();

    @FXML
    private ComboBox<String> fromComboBox;

    @FXML
    private ComboBox<String> toComboBox;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private CheckBox connectingFlightsCheckBox;

    @FXML
    private Spinner<Integer> passengerCountSpinner;

    @FXML
    private ListView<String> resultsListView;

    @FXML
    public void initialize() {
        // Fylla drop-down lista ur DB.
        loadAirportOptions();

        sortComboBox.setItems(FXCollections.observableArrayList(
                "Price",
                "Duration",
                "Departure time",
                "Arrival time",
                "Available seats"
        ));

        sortComboBox.setValue("Price");

        // Skynsamleg defaults fyrir fyrsta search.
        fromDatePicker.setValue(LocalDate.now());
        toDatePicker.setValue(LocalDate.now().plusDays(7));
        passengerCountSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1));

        resultsListView.setItems(FXCollections.observableArrayList());
    }

    @FXML
    private void handleSearch() {
        String from = fromComboBox.getValue();
        String to = toComboBox.getValue();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();
        int passengerCount = passengerCountSpinner.getValue();
        String sortBy = sortComboBox.getValue();
        boolean allowConnectingFlights = connectingFlightsCheckBox.isSelected();

        // Grunnvalidering i UI adur en controller/repository er kallad.
        if (from == null || to == null) {
            resultsListView.setItems(FXCollections.observableArrayList(
                    "Please choose both airports."
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

        searchFlights(from, to, fromDate, toDate, passengerCount, sortBy, allowConnectingFlights);
    }

    @FXML
    private void handleBookFlight() {
        String selectedLine = resultsListView.getSelectionModel().getSelectedItem();
        if (selectedLine == null || selectedLine.isBlank()) {
            resultsListView.setItems(FXCollections.observableArrayList(
                    "Please select a direct flight result before booking."
            ));
            return;
        }

        // Bokanlegt bara fyrir direct flight rows (ekki textual itinerary row).
        Flight selectedFlight = directFlightsByResultLine.get(selectedLine);
        if (selectedFlight == null) {
            resultsListView.setItems(FXCollections.observableArrayList(
                    "Selected row is not a direct flight. Please select a direct flight line with a flight number."
            ));
            return;
        }

        int passengerCount = passengerCountSpinner.getValue();
        String flightNumber = selectedFlight.getFlightNumber();

        // UI check fyrst svo notandi fai strax skilabod.
        int availableBefore = flightController.getAvailableSeatCount(flightNumber);
        if (availableBefore < passengerCount) {
            resultsListView.setItems(FXCollections.observableArrayList(
                    "Not enough seats for this booking. Requested: " + passengerCount + ", available: " + availableBefore
            ));
            return;
        }

        try {
            Itinerary itinerary = new Itinerary(
                    "UI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                    0,
                    0.0,
                    new ArrayList<>(List.of(selectedFlight))
            );

            Reservation reservation = reservationController.createReservation(itinerary);

            // Bua til demo passengers i samraemi vid valinn passengerCount.
            for (int i = 1; i <= passengerCount; i++) {
                Passenger passenger = new Passenger(
                        "Demo Passenger " + i,
                        buildDemoEmail(reservation.getReservationCode(), i),
                        "+354-555-0000",
                        "IS",
                        Date.from(Instant.parse("1990-01-01T00:00:00Z"))
                );

                ReservationItem item = reservationController.addPassenger(reservation.getReservationCode(), passenger);
                assignFirstAvailableSeat(reservation.getReservationCode(), item.getItemId(), selectedFlight);
            }

            reservationController.confirmReservation(reservation.getReservationCode());

            handleSearch();
            resultsListView.getItems().add(
                    0,
                    "Booked " + passengerCount + " seat(s) on " + flightNumber + " | Reservation: " + reservation.getReservationCode()
            );
        } catch (Exception e) {
            String details = e.getMessage();
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                details = e.getCause().getMessage();
            }
            resultsListView.setItems(FXCollections.observableArrayList(
                    "Booking failed.",
                    details
            ));
        }
    }

    private void searchFlights(String from, String to,
                LocalDate fromDate, LocalDate toDate, int passengerCount,
                String sortBy, boolean allowConnectingFlights) {

        try {
            ZoneId zone = ZoneId.of("UTC");

            ZonedDateTime startDateTime = fromDate.atStartOfDay(zone);
            ZonedDateTime endDateTime = toDate.atStartOfDay(zone);

            // Search pipeline: route/date (+ seat threshold) -> date-range filter.
            List<Flight> flights = flightController.searchFlights(from, to, startDateTime, passengerCount);

            flights = flightController.filterByDepartureTimeRange(flights, startDateTime, endDateTime);

            Map<String, Integer> availableSeatsByFlight = loadAvailableSeatCounts(flights);
            directFlightsByResultLine.clear();

            // Sort val i UI er mapad i mismunandi comparatora.
            if ("Price".equals(sortBy)) {
                flights = flightController.sortByPrice(flights);
            } else if ("Duration".equals(sortBy)) {
                flights.sort((a, b) -> Integer.compare(a.getDurationMinutes(), b.getDurationMinutes()));
            } else if ("Departure time".equals(sortBy)) {
                flights.sort((a, b) -> a.getDepartureDateTime().compareTo(b.getDepartureDateTime()));
            } else if ("Arrival time".equals(sortBy)) {
                flights.sort((a, b) -> a.getArrivalDateTime().compareTo(b.getArrivalDateTime()));
            } else if ("Available seats".equals(sortBy)) {
                flights.sort((a, b) -> Integer.compare(
                        availableSeatsByFlight.getOrDefault(b.getFlightNumber(), 0),
                        availableSeatsByFlight.getOrDefault(a.getFlightNumber(), 0)
                ));
            }

            List<String> results = new ArrayList<>();

            // Vista tengingu milli birtrar textalinu og underlying Flight fyrir booking takka.
            for (Flight flight : flights) {
                int availableSeats = availableSeatsByFlight.getOrDefault(flight.getFlightNumber(), 0);
                String row = formatFlight(flight, availableSeats);
                results.add(row);
                directFlightsByResultLine.put(row, flight);
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

    private String formatFlight(Flight flight, int availableSeats) {
        return flight.getDepartureAirport().getAirportCode() + " -> "
                + flight.getArrivalAirport().getAirportCode()
                + " | Flight: " + flight.getFlightNumber()
                + " | Departure: " + flight.getDepartureDateTime().toLocalDateTime()
                + " | Arrival: " + flight.getArrivalDateTime().toLocalDateTime()
                + " | Duration: " + flight.getDurationMinutes() + " min"
                + " | Price: " + flight.getBasePrice()
                + " | Seats: " + availableSeats
                + " | Status: " + flight.getStatus();
    }

    private Map<String, Integer> loadAvailableSeatCounts(List<Flight> flights) {
        // Saetafjoldi lesinn ur controller/repository fyrir hverja nidurstodu.
        Map<String, Integer> availableSeatsByFlight = new HashMap<>();
        for (Flight flight : flights) {
            int available = flightController.getAvailableSeatCount(flight.getFlightNumber());
            availableSeatsByFlight.put(flight.getFlightNumber(), available);
        }
        return availableSeatsByFlight;
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

    private void loadAirportOptions() {
        // Combo boxes fylltar med airport codum ur airports toflu.
        List<Airport> airports = airportRepository.findAll();
        List<String> airportCodes = new ArrayList<>();
        for (Airport airport : airports) {
            airportCodes.add(airport.getAirportCode());
        }

        fromComboBox.setItems(FXCollections.observableArrayList(airportCodes));
        toComboBox.setItems(FXCollections.observableArrayList(airportCodes));

        if (!airportCodes.isEmpty()) {
            fromComboBox.setValue(airportCodes.get(0));
        }
        if (airportCodes.size() > 1) {
            toComboBox.setValue(airportCodes.get(1));
        } else if (!airportCodes.isEmpty()) {
            toComboBox.setValue(airportCodes.get(0));
        }
    }

    private String buildDemoEmail(String reservationCode, int passengerIndex) {
        return "demo+" + reservationCode.toLowerCase() + "+" + passengerIndex + "@example.local";
    }

    private void assignFirstAvailableSeat(String reservationCode, String itemId, Flight flight) {
        // Reynir S1..Sn i rod thar til seat assignment tekst.
        RuntimeException lastSeatError = null;
        for (int seatNumber = 1; seatNumber <= flight.getCapacity(); seatNumber++) {
            String seatId = "S" + seatNumber;
            try {
                reservationController.assignSeat(
                        reservationCode,
                        itemId,
                        flight.getFlightNumber(),
                        seatId
                );
                return;
            } catch (IllegalArgumentException e) {
                String msg = e.getMessage();
                if (msg != null && msg.contains("Seat already assigned")) {
                    lastSeatError = e;
                    continue;
                }
                throw e;
            }
        }

        if (lastSeatError != null) {
            throw new IllegalStateException(
                    "Could not find an available seat on flight " + flight.getFlightNumber(),
                    lastSeatError
            );
        }
        throw new IllegalStateException(
                "Could not find an available seat on flight " + flight.getFlightNumber()
        );
    }
}
