package rent.dto;

public class SharedRoomBookingDayInfoDto {
    private int currentGuests;
    private int arriveGuests;
    private int departureGuests;

    public SharedRoomBookingDayInfoDto(int currentGuests, int arriveGuests, int departureGuests) {
        this.currentGuests = currentGuests;
        this.arriveGuests = arriveGuests;
        this.departureGuests = departureGuests;
    }

    public int getCurrentGuests() {
        return currentGuests;
    }

    public void plusCurrentGuests(int currentGuests) {
        this.currentGuests += currentGuests;
    }

    public int getArriveGuests() {
        return arriveGuests;
    }

    public void plusArriveGuests(int arriveGuests) {
        this.arriveGuests += arriveGuests;
    }

    public int getDepartureGuests() {
        return departureGuests;
    }

    public void plusDepartureGuests(int departureGuests) {
        this.departureGuests += departureGuests;
    }
}
