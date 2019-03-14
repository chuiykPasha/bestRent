package rent.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "Apartment")
public class Apartment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String location;

    private float price;

    private int maxNumberOfGuests;

    @Column(length = 100)

    private String title;

    private double latitude;

    private double longitude;

    @ManyToOne
    @JoinColumn(name = "typeOfHouseId", nullable = false)
    private TypeOfHouse typeOfHouse;

    @ManyToOne
    @JoinColumn(name = "availableToGuestId", nullable = false)
    private AvailableToGuest availableToGuest;

    @OneToMany(mappedBy = "apartment")
    @Fetch(FetchMode.JOIN)
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

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    private int numberOfRooms;

    @OneToMany(mappedBy = "apartment")
    private Set<Room> rooms = new HashSet<>();

    private boolean isActive;

    public Apartment(Integer id) {
        this.id = id;
    }

    public Apartment(String description, String location, float price, int maxNumberOfGuests, TypeOfHouse typeOfHouse,
                     AvailableToGuest availableToGuest, Set<ApartmentComfort> apartmentComforts, String title, User user,
                     double longitude, double latitude, int numberOfRooms) {
        this.description = description;
        this.location = location;
        this.price = price;
        this.maxNumberOfGuests = maxNumberOfGuests;
        this.typeOfHouse = typeOfHouse;
        this.availableToGuest = availableToGuest;
        this.apartmentComforts = apartmentComforts;
        this.title = title;
        this.user = user;
        this.longitude = longitude;
        this.latitude = latitude;
        this.numberOfRooms = numberOfRooms;
        this.isActive = true;
    }
}
