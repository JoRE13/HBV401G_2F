package src.main.java.airline.model;

public class Airplane {
    private String type;
    private int numSeats;

    public Airplane(String type, int numSeats) {
        this.type = type;
        this.numSeats = numSeats;
    }

    //getters
    public String getType(){
        return type;
    }

    public int getNumSeats(){
        return numSeats;
    }
}
