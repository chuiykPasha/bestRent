package rent.form;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import rent.entities.ApartmentComfort;
import rent.entities.AvailableToGuest;
import rent.entities.TypeOfHouse;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ApartmentInfoForm implements Serializable{
    private Integer id;

    @NotEmpty
    private String description;
    @NotNull
    @Min(1)
    private BigDecimal price = new BigDecimal(1);

    private int maxNumberOfGuests = 1;

    private Integer typeOfHouseId;

    private Integer availableToGuestId;

    private List<Integer> selectedComforts;

    @Length(min = 10, max = 100)
    private String title;

    private List<TypeOfHouse> typeOfHouses;

    private List<AvailableToGuest> availableToGuests;

    private List<ApartmentComfort> comforts;

    private int numberOfRooms = 1;

    private List<Integer> guestsInRoom;
}
