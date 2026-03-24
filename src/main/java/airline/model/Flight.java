package src.main.java.airline.model;

import src.main.java.airline.util.DateUtils;

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
    private Airplane airplane;

    private List<Seat> seats;

    //constructor
    public Flight(
            String flightNumber,
            ZonedDateTime departureDateTime,
            ZonedDateTime arrivalDateTime,
            double basePrice,
            FlightStatus status,
            int capacity,
            Airport arrivalAirport,
            Airplane airplane) {
        this.flightNumber = flightNumber;
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
        this.durationMinutes = (int) DateUtils.zonedDateTimeDifference(departureDateTime, arrivalDateTime,
                ChronoUnit.SECONDS);
        this.basePrice = basePrice;
        this.capacity = capacity;
        this.status = FlightStatus.SCHEDULED;
        this.arrivalAirport = arrivalAirport;
        this.airplane = airplane;
        this.seats = new ArrayList<>();

        //einfaldari útgáfa af sætunum en lokaútgáfa ætti að vera
        for (int i = 1; i <= airplane.getNumSeats(); i++) {
            this.seats.add(new Seat("S" + i, SeatType.MIDDLE));

        }


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

    //methods
    public boolean hasDeparted() {
        return status == FlightStatus.DEPARTED;
    }

    public boolean hasArrived(){
        return status == FlightStatus.ARRIVED;
    }

    public int getAvailableSeatCount(){
        int count = 0;
        for(Seat seata : seats){
            if(seata.isAvailable()) count++;
        }
        return count;
    }

    public List<Seat> listAvailableSeats(){
        List<Seat> available = new ArrayList<>();
        for (Seat seat : seats) {
            if(seat.isAvailable()) available.add(seat);
        }
        return available;
    }

    public boolean isDirectTo(Airport arrivalAirport){
        return true;
    }

    public double calculatePrice(int numPassengers){
        return basePrice * numPassengers;
    }

    public void addSeat(Seat seat){
        seats.add(seat);
    }



    public static void main(String[] args) {
        ZonedDateTime depDT = ZonedDateTime.of(2026, 9, 9, 18, 0, 0, 0, ZoneId.of("GMT+2"));

    }
}
