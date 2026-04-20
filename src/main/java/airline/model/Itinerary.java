package airline.model;

import java.util.List;

public class Itinerary {
    // Itinerary geymir leg listann + tolur (duration/price).
    private String itineraryId;
    private int totalDuration;
    private double totalPrice;
    private List<Flight> legs;

    public Itinerary(
            String itineraryId,
            int totalDuration,
            double totalPrice,
            List<Flight> legs){
        this.itineraryId = itineraryId;
        this.totalDuration = totalDuration;
        this.totalPrice = totalPrice;
        this.legs = legs;
    }

    //getters
    public String getItineraryId(){
        return itineraryId;
    }

    public int getTotalDuration(){
        return totalDuration;
    }

    public double getTotalPrice(){
        return totalPrice;
    }

    public List<Flight> getLegs(){
        return legs;
    }

    //methods
    public void addLeg(Flight flight){
        // Bætir vid einum legg i ferdina.
        legs.add(flight);
    }

    public int computeTotalDuration(){
        // Reiknum aftur fra grunni til ad forðast stale gildi.
        totalDuration = 0;

        for(Flight flight : legs){
            totalDuration += flight.getDurationMinutes();
        }

        return totalDuration;

    }

    public double computeTotalPrice(){
        // Sama regla fyrir verd: summa yfir alla leggi.
        totalPrice = 0;

        for (Flight flight : legs) {
            totalPrice += flight.getBasePrice();
        }

        return totalPrice;
    }


    public static void main(String[] args) {

    }
}
