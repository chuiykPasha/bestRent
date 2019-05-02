package rent.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class BookingDto {
    List<LocalDate> reservedDates;
    private String message;

    public BookingDto(String message) {
        this.message = message;
    }

    public BookingDto(List<LocalDate> reservedDates, String message) {
        this.reservedDates = reservedDates;
        this.message = message;
    }
}
