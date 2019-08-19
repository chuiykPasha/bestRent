package rent.service.booking;

public enum BookingType {
    SHARED_ROOM("Shared room"), ENTIRE_APARTMENT("Entire apartment"), PRIVATE_ROOM("Private room");

    private String type;

    BookingType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
