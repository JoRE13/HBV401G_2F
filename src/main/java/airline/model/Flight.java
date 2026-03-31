package airline.model;

import airline.util.DateUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Flight {
    private String flightNumber;
    private ZonedDateTime departureDateTime;
    private ZonedDateTime arrivalDateTime;
    private int durationMinutes;
    private double basePrice;
    private FlightStatus status;
    private int capacity;
    private Airport arrivalAirport;
    private Airport departureAirport;
    private String airplaneType;

    private List<Seat> seats;

    // constructor
    public Flight(
            String flightNumber,
            ZonedDateTime departureDateTime,
            ZonedDateTime arrivalDateTime,
            double basePrice,
            FlightStatus status,
            int capacity,
            Airport arrivalAirport,
            Airport departureAirport,
            String airplaneType) {
        this.flightNumber = flightNumber;
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
        this.durationMinutes = (int) DateUtils.zonedDateTimeDifference(
                departureDateTime,
                arrivalDateTime,
                ChronoUnit.MINUTES
        );
        this.basePrice = basePrice;
        this.capacity = capacity;
        this.status = status;
        this.arrivalAirport = arrivalAirport;
        this.departureAirport = departureAirport;
        this.airplaneType = (airplaneType == null || airplaneType.isBlank()) ? "UNSPECIFIED" : airplaneType;
        this.seats = new ArrayList<>();

        // einfaldari utgafa af saetunum en lokautgafa aetti ad vera
        for (int i = 1; i <= capacity; i++) {
            this.seats.add(new Seat("S" + i, SeatType.MIDDLE));
        }
    }

    // getters
    public double getBasePrice() {
        return basePrice;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getAirplaneType() {
        return airplaneType;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public Airport getDepartureAirport() {
        return departureAirport;
    }

    public Airport getArrivalAirport() {
        return arrivalAirport;
    }

    public ZonedDateTime getDepartureDateTime() {
        return departureDateTime;
    }

    public ZonedDateTime getArrivalDateTime() {
        return arrivalDateTime;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    // setters
    public void setStatusCancelled() {
        status = FlightStatus.CANCELLED;
    }

    // methods
    public boolean hasDeparted() {
        return status == FlightStatus.DEPARTED;
    }

    public boolean hasArrived() {
        return status == FlightStatus.ARRIVED;
    }

    public void reschedule(
            ZonedDateTime departureDateTime,
            ZonedDateTime arrivalDateTime) {
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;

        this.durationMinutes = (int) DateUtils.zonedDateTimeDifference(
                departureDateTime,
                arrivalDateTime,
                ChronoUnit.MINUTES
        );
    }

    public int getAvailableSeatCount() {
        int count = 0;
        for (Seat seat : seats) {
            if (seat.isAvailable()) {
                count++;
            }
        }
        return count;
    }

    public List<Seat> listAvailableSeats() {
        List<Seat> available = new ArrayList<>();
        for (Seat seat : seats) {
            if (seat.isAvailable()) {
                available.add(seat);
            }
        }
        return available;
    }

    public boolean isDirectTo(Airport arrivalAirport) {
        if (arrivalAirport == null) {
            return false;
        }
        if (this.arrivalAirport == null
                || this.arrivalAirport.getAirportCode() == null
                || arrivalAirport.getAirportCode() == null) {
            return false;
        }
        return this.arrivalAirport.getAirportCode().equalsIgnoreCase(arrivalAirport.getAirportCode());
    }

    public double calculatePrice(int numPassengers) {
        return basePrice * numPassengers;
    }

    public void addSeat(Seat seat) {
        seats.add(seat);
    }

    public static void main(String[] args) {
        ZonedDateTime depDT = ZonedDateTime.of(2026, 9, 9, 18, 0, 0, 0, ZoneId.of("GMT+2"));
    }
}