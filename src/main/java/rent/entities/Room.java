package rent.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "room")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int maxNumberOfGuests;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "apartmentId", nullable = false)
    private Apartment apartment;

    @JsonIgnore
    @OneToMany(mappedBy = "room")
    private Set<ApartmentCalendar> calendars = new HashSet<>();

    public Room(int maxNumberOfGuests, Apartment apartment) {
        this.maxNumberOfGuests = maxNumberOfGuests;
        this.apartment = apartment;
    }

    public Room(Integer id, int maxNumberOfGuests) {
        this.id = id;
        this.maxNumberOfGuests = maxNumberOfGuests;
    }
}
