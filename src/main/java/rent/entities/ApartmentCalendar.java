package rent.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ApartmentCalendar")
public class ApartmentCalendar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Date arrival;

    @Column(nullable = false)
    private Date departure;

    private boolean firstDayFree;

    private boolean lastDayFree;

    private int currentCountGuest;

    @ManyToOne
    @JoinColumn(name = "apartmentId", nullable = false)
    private Apartment apartment;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "roomId")
    private Room room;

    private boolean isCanceled;

    private float price;

    public ApartmentCalendar(Date arrival, Date departure, Apartment apartment, boolean firstDayFree, boolean lastDayFree, int currentCountGuest, User user, float price) {
        this.arrival = arrival;
        this.departure = departure;
        this.apartment = apartment;
        this.firstDayFree = firstDayFree;
        this.lastDayFree = lastDayFree;
        this.currentCountGuest = currentCountGuest;
        this.user = user;
        this.isCanceled = false;
        this.price = price;
    }

    public ApartmentCalendar(Date arrival, Date departure, boolean firstDayFree, boolean lastDayFree, int currentCountGuest, Apartment apartment, User user, Room room, boolean isCanceled, float price) {
        this.arrival = arrival;
        this.departure = departure;
        this.firstDayFree = firstDayFree;
        this.lastDayFree = lastDayFree;
        this.currentCountGuest = currentCountGuest;
        this.apartment = apartment;
        this.user = user;
        this.room = room;
        this.isCanceled = isCanceled;
        this.price = price;
    }
}
