package airline.model;

public class ReservationItem {
    // Eitt item er einn farthi i einni reservation.
    private String itemId;
    private double pricePaid;
    private String passengerEmail;

    // Constructor fyrir reservation item sem tengist passenger med email.
    public ReservationItem(
            String itemId,
            double pricePaid,
            String passengerEmail){
        this.itemId = itemId;
        this.pricePaid = pricePaid;
        this.passengerEmail = passengerEmail;
    }

    //getters
    public String getItemId(){
        return itemId;
    }

    public double getPricePaid() {
        return pricePaid;
    }

    public String getPassengerEmail() {
        return passengerEmail;
    }

    //setters
    public void setItemId(String itemId){
        this.itemId = itemId;
    }

    public void setPricePaid(double pricePaid) {
        this.pricePaid = pricePaid;
    }

    public void setPassengerEmail(String passengerEmail) {
        this.passengerEmail = passengerEmail;
    }

    public static void main(String[] args) {

    }
}
