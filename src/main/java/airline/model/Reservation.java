package airline.model;

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
        this.status = (status == null) ? ReservationStatus.PENDING : status;
        this.totalPrice = totalPrice;
    }

    //getters
    public String getReservationCode(){
        return reservationCode;
    }

    public Date getCreatedAt(){
        return createdAt;
    }

    public ReservationStatus getStatus(){
        return status;
    }

    public double getTotalPrice(){
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
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
