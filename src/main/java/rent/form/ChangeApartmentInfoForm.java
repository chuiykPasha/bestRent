package rent.form;

import org.hibernate.validator.constraints.Length;
import rent.entities.ApartmentComfort;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class ChangeApartmentInfoForm {
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getMaxNumberOfGuests() {
        return maxNumberOfGuests;
    }

    public void setMaxNumberOfGuests(int maxNumberOfGuests) {
        this.maxNumberOfGuests = maxNumberOfGuests;
    }

    public List<Integer> getSelectedComforts() {
        return selectedComforts;
    }

    public void setSelectedComforts(List<Integer> selectedComforts) {
        this.selectedComforts = selectedComforts;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ApartmentComfort> getComforts() {
        return comforts;
    }

    public void setComforts(List<ApartmentComfort> comforts) {
        this.comforts = comforts;
    }

    public int getNumberOfRooms() {
        return numberOfRooms;
    }

    public void setNumberOfRooms(int numberOfRooms) {
        this.numberOfRooms = numberOfRooms;
    }

    public List<Integer> getGuestsInRoom() {
        return guestsInRoom;
    }

    public void setGuestsInRoom(List<Integer> guestsInRoom) {
        this.guestsInRoom = guestsInRoom;
    }
}
