package src.main.java.airline.model;

import java.util.Date;

public class Reservation {
    private String reservationCode;
    private Date createdAt;
    private ReservationStatus status;
    private double totalPrice;

    //constructor
    public Reservation(
            String reservationCode,
            Date createdAt,
            ReservationStatus status,
            double totalPrice){
        this.reservationCode = reservationCode;
        this.createdAt = createdAt;
        this.status = ReservationStatus.PENDING; //pending í upphafi reservation
        this.totalPrice = totalPrice;
    }

    //getters
    public String getReservationCode(){
        return reservationCode;
    }

    public Date getCreatedAt(){
        return createdAt;
    }

    public ReservationStatus status(){
        return status;
    }

    public double getTotalPrice(){
        return totalPrice;
    }

    //methods
    public void confirmReservation(){
        status = ReservationStatus.CONFIRMED;
    }

    public void cancelReservation(){
        status = ReservationStatus.CANCELLED;
    }

    public static void main(String[] args) {

    }
}
