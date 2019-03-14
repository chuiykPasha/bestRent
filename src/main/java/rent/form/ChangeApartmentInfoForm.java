package rent.form;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import rent.entities.ApartmentComfort;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Getter
@Setter
public class ChangeApartmentInfoForm implements Serializable {
    private Integer id;

    @NotEmpty
    private String description;

    @Min(1)
    private BigDecimal price;

    private int maxNumberOfGuests;

    private List<Integer> selectedComforts;

    @Length(min = 10, max = 100)
    private String title;

    private List<ApartmentComfort> comforts;

    private int numberOfRooms;

    private List<Integer> guestsInRoom;

    public ChangeApartmentInfoForm(Integer id, @NotEmpty String description, @Min(1) BigDecimal price, int maxNumberOfGuests, @Length(min = 10, max = 100) String title,
                                   List<ApartmentComfort> comforts, int numberOfRooms) {
        this.id = id;
        this.description = description;
        this.price = price.setScale(2, RoundingMode.CEILING);
        this.maxNumberOfGuests = maxNumberOfGuests;
        this.title = title;
        this.comforts = comforts;
        this.numberOfRooms = numberOfRooms;
    }
}
