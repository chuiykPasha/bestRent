package rent.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public class BookingDto implements Serializable {
    List<LocalDate> reservedDates;
    private String message;

    public BookingDto(String message) {
        this.message = message;
    }

    public BookingDto(List<LocalDate> reservedDates, String message) {
        this.reservedDates = reservedDates;
        this.message = message;
    }

    public List<LocalDate> getReservedDates() {
        return reservedDates;
    }

    public void setReservedDates(List<LocalDate> reservedDates) {
        this.reservedDates = reservedDates;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
