package rent.entities;

import javax.persistence.*;

@Entity
@Table(name = "room")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int maxNumberOfGuests;

    @ManyToOne
    @JoinColumn(name = "apartmentId", nullable = false)
    private Apartment apartment;

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
}
