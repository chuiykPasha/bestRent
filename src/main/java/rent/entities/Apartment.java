package rent.entities;

import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Indexed
@Entity
@Table(name = "Apartment")
public class Apartment implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String description;
    @Field(index = Index.YES, analyze= Analyze.YES, store= Store.NO)
    private String location;
    @Column
    private float price;
    @Column
    private int maxNumberOfGuests;
    @Column(length = 100)
    private String title;

    @ManyToOne
    @JoinColumn(name = "typeOfHouseId", nullable = false)
    private TypeOfHouse typeOfHouse;

    @ManyToOne
    @JoinColumn(name = "availableToGuestId", nullable = false)
    private AvailableToGuest availableToGuest;

    @OneToMany(mappedBy = "apartment")
    private Set<ApartmentImage> images = new HashSet<>();

    @OneToMany(mappedBy = "apartment")
    private Set<ApartmentCalendar> calendars = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "Apartment_ApartmentComfort",
            joinColumns = {@JoinColumn(name = "apartmentId")},
            inverseJoinColumns = { @JoinColumn(name = "apartmentComfortId")}
    )
    private Set<ApartmentComfort> apartmentComforts = new HashSet<>();

    public Apartment() {}

    public Apartment(Integer id) {
        this.id = id;
    }

    public Apartment(String description, String location, float price, int maxNumberOfGuests, TypeOfHouse typeOfHouse, AvailableToGuest availableToGuest, Set<ApartmentComfort> apartmentComforts, String title) {
        this.description = description;
        this.location = location;
        this.price = price;
        this.maxNumberOfGuests = maxNumberOfGuests;
        this.typeOfHouse = typeOfHouse;
        this.availableToGuest = availableToGuest;
        this.apartmentComforts = apartmentComforts;
        this.title = title;
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

    public TypeOfHouse getTypeOfHouse() {
        return typeOfHouse;
    }

    public void setTypeOfHouse(TypeOfHouse typeOfHouse) {
        this.typeOfHouse = typeOfHouse;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
