package src.main.java.airline.model;

import src.main.java.airline.util.DateUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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
            FlightStatus status,
            int capacity) {
        this.flightNumber = flightNumber;
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
        this.durationMinutes = (int) DateUtils.zonedDateTimeDifference(departureDateTime, arrivalDateTime,
                ChronoUnit.SECONDS);
        this.basePrice = basePrice;
        this.capacity = capacity;
    }

    // þurfum við kannski að bæta við plane inn í módelið sem skilgreinir hvernig
    // vél, hversu mörg sæti o.s.frv.

    //getters
    public double getBasePrice(){
        return basePrice;
    }

    public int getDurationMinutes(){
        return durationMinutes;
    }



    public static void main(String[] args) {
        ZonedDateTime depDT = ZonedDateTime.of(2026, 9, 9, 18, 0, 0, 0, ZoneId.of("GMT+2"));

    }
}
