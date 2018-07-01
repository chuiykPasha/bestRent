package rent.form;

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

    public Integer getTypeOfHouseId() {
        return typeOfHouseId;
    }

    public void setTypeOfHouseId(Integer typeOfHouseId) {
        this.typeOfHouseId = typeOfHouseId;
    }

    public Integer getAvailableToGuestId() {
        return availableToGuestId;
    }

    public void setAvailableToGuestId(Integer availableToGuestId) {
        this.availableToGuestId = availableToGuestId;
    }

    public List<Integer> getSelectedComforts() {
        return selectedComforts;
    }

    public void setSelectedComforts(List<Integer> selectedComforts) {
        this.selectedComforts = selectedComforts;
    }

    public List<TypeOfHouse> getTypeOfHouses() {
        return typeOfHouses;
    }

    public void setTypeOfHouses(List<TypeOfHouse> typeOfHouses) {
        this.typeOfHouses = typeOfHouses;
    }

    public List<AvailableToGuest> getAvailableToGuests() {
        return availableToGuests;
    }

    public void setAvailableToGuests(List<AvailableToGuest> availableToGuests) {
        this.availableToGuests = availableToGuests;
    }

    public List<ApartmentComfort> getComforts() {
        return comforts;
    }

    public void setComforts(List<ApartmentComfort> comforts) {
        this.comforts = comforts;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
