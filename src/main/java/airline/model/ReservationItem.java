package airline.model;

public class ReservationItem {
    private String itemId;
    private double pricePaid;

    public ReservationItem(
            String itemId,
            double pricePaid){
        this.itemId = itemId;
        this.pricePaid = pricePaid;
    }

    //getters
    public String getItemId(){
        return itemId;
    }

    public double getPricePaid() {
        return pricePaid;
    }

    //setters
    public void setItemId(String itemId){
        this.itemId = itemId;
    }
    public void setPricePaid(double pricePaid) {
        this.pricePaid = pricePaid;
    }

    public static void main(String[] args) {

    }
}
