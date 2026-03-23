package src.main.java.airline.model;

public class Seat {
    private String seatID;
    private String seatType;
    private Boolean isAvailable;

    public void Seat(String seatID, String seatType) {
        this.seatID = seatID;
        this.seatType = seatType;
        this.isAvailable = true;
    }

    public Boolean isWindow() {
        // gera is window, hvernig skilgreinum við seatType, kannski enum?
        // enum er góð hugmynd
        return false;
    }

    public Boolean isAisle() {
        // gera is aisle, hvernig skilgreinum við seatType, kannski enum?
        return false;
    }
}
