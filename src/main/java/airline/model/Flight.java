package src.main.java.airline.model;

import java.time.*;
import src.main.java.airline.util.DateUtils;
import java.time.temporal.ChronoUnit;

public class Flight {
    private String flightNumber;
    private ZonedDateTime departureDateTime;
    private ZonedDateTime arrivalDateTime;
    private int durationMinutes;
    private double basePrice;
    private FlightStatus status;
    private int capacity;

    public Flight(
            String flightNumber,
            ZonedDateTime departureDateTime,
            ZonedDateTime arrivalDateTime,
            double basePrice,
            FlightStatus status) {
        this.flightNumber = flightNumber;
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
        this.durationMinutes = (int) DateUtils.zonedDateTimeDifference(departureDateTime, arrivalDateTime,
                ChronoUnit.SECONDS);
        this.basePrice = basePrice;
        this.capacity = getCapacity();
    }

    private int getCapacity() {
        return 0;
    }

    public static void main(String[] args) {
        ZonedDateTime depDT = ZonedDateTime.of(2026, 9, 9, 18, 0, 0, 0, ZoneId.of("GMT+2"));

    }
}
