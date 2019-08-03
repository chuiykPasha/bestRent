package rent.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class BookingResultDto {
    List<LocalDate> reservedDates;
    private String message;

    public BookingResultDto(String message) {
        this.message = message;
    }

    public BookingResultDto(List<LocalDate> reservedDates, String message) {
        this.reservedDates = reservedDates;
        this.message = message;
    }
}
