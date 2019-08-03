package rent.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import rent.entities.User;

import java.sql.Date;

@Getter
@Builder
public class BookingInfoDto {
    private int apartmentId;
    private Date startDate;
    private java.sql.Date endDate;
    private int guestsCount;
    private User user;
    private float price;
    private int maxNumberOfGuests;
    private int numberOfRooms;
}
