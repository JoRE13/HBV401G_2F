package src.main.java.airline.model;

public class Seat {
    private String seatID;
    private SeatType seatType;
    private boolean isAvailable;

    //constructor
    public Seat(String seatID, SeatType seatType) {
        this.seatID = seatID;
        this.seatType = seatType;
        this.isAvailable = true;
    }

    public boolean isWindow() {
        // gera is window, hvernig skilgreinum við seatType, kannski enum?
        // enum er góð hugmynd
        return seatType == SeatType.WINDOW;
    }

    public boolean isAisle() {
        // gera is aisle, hvernig skilgreinum við seatType, kannski enum?
        return seatType == SeatType.AISLE;
    }

    //Taka frá sæti og kanna hvort sé laust:

    public boolean isAvailable(){
        return isAvailable;
    }

    public void reserve(){
        isAvailable = false;
    }
}
