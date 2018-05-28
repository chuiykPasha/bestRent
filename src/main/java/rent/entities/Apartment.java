package rent.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Apartment")
public class Apartment implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private String location;
    @Column
    private float price;
    @Column
    private int maxNumberOfGuests;

    @ManyToOne
    @JoinColumn(name = "typeOfHousingId", nullable = false)
    private TypeOfHousing typeOfHousing;

    @ManyToOne
    @JoinColumn(name = "availableToGuestId", nullable = false)
    private AvailableToGuest availableToGuest;

    @OneToMany(mappedBy = "apartment")
    private Set<ApartmentImage> images = new HashSet<>();

    @OneToMany(mappedBy = "apartment")
    private Set<ApartmentCalendar> calendars = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(
            name = "Apartment_ApartmentComfort",
            joinColumns = {@JoinColumn(name = "apartmentId")},
            inverseJoinColumns = { @JoinColumn(name = "apartmentComfortId")}
    )
    private Set<ApartmentComfort> apartmentComforts = new HashSet<>();

    public Apartment() {}

    public Apartment(String description, String location, float price, int maxNumberOfGuests, TypeOfHousing typeOfHousing, AvailableToGuest availableToGuest) {
        this.description = description;
        this.location = location;
        this.price = price;
        this.maxNumberOfGuests = maxNumberOfGuests;
        this.typeOfHousing = typeOfHousing;
        this.availableToGuest = availableToGuest;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getMaxNumberOfGuests() {
        return maxNumberOfGuests;
    }

    public void setMaxNumberOfGuests(int maxNumberOfGuests) {
        this.maxNumberOfGuests = maxNumberOfGuests;
    }

    public TypeOfHousing getTypeOfHousing() {
        return typeOfHousing;
    }

    public void setTypeOfHousing(TypeOfHousing typeOfHousing) {
        this.typeOfHousing = typeOfHousing;
    }

    public AvailableToGuest getAvailableToGuest() {
        return availableToGuest;
    }

    public void setAvailableToGuest(AvailableToGuest availableToGuest) {
        this.availableToGuest = availableToGuest;
    }

    public Set<ApartmentImage> getImages() {
        return images;
    }

    public void setImages(Set<ApartmentImage> images) {
        this.images = images;
    }

    public Set<ApartmentCalendar> getCalendars() {
        return calendars;
    }

    public void setCalendars(Set<ApartmentCalendar> calendars) {
        this.calendars = calendars;
    }

    public Set<ApartmentComfort> getApartmentComforts() {
        return apartmentComforts;
    }

    public void setApartmentComforts(Set<ApartmentComfort> apartmentComforts) {
        this.apartmentComforts = apartmentComforts;
    }
}
