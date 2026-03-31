package airline.model;

public class Seat {
    private String seatId;
    private SeatType seatType;
    private boolean isAvailable;

    // constructor
    public Seat(String seatId, SeatType seatType) {
        this.seatId = seatId;
        this.seatType = seatType;
        this.isAvailable = true;
    }

    public String getSeatId() {
        return seatId;
    }

    public SeatType getSeatType() {
        return seatType;
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

    // Taka frá sæti og kanna hvort sé laust:
    public boolean isAvailable() {
        return isAvailable;
    }

    public void reserve() {
        isAvailable = false;
    }

    // gera sæti laust aftur
    public void release() {
        isAvailable = true;
    }
}
