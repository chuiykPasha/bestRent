package rent.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "room")
public class Room implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int maxNumberOfGuests;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "apartmentId", nullable = false)
    private Apartment apartment;

    @OneToMany(mappedBy = "room")
    private Set<ApartmentCalendar> calendars = new HashSet<>();

    public Room(){}

    public Room(int maxNumberOfGuests, Apartment apartment) {
        this.maxNumberOfGuests = maxNumberOfGuests;
        this.apartment = apartment;
    }

    public Room(Integer id, int maxNumberOfGuests) {
        this.id = id;
        this.maxNumberOfGuests = maxNumberOfGuests;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getMaxNumberOfGuests() {
        return maxNumberOfGuests;
    }

    public void setMaxNumberOfGuests(int maxNumberOfGuests) {
        this.maxNumberOfGuests = maxNumberOfGuests;
    }

    public Apartment getApartment() {
        return apartment;
    }

    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }

    public Set<ApartmentCalendar> getCalendars() {
        return calendars;
    }

    public void setCalendars(Set<ApartmentCalendar> calendars) {
        this.calendars = calendars;
    }
}
