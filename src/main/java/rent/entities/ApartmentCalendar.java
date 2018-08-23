package rent.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;

@Entity
@Table(name = "ApartmentCalendar")
public class ApartmentCalendar implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private Date arrival;
    @Column(nullable = false)
    private Date departure;
    @Column
    private boolean firstDayFree;
    @Column
    private boolean lastDayFree;
    @Column
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

    public ApartmentCalendar() {}

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getArrival() {
        return arrival;
    }

    public void setArrival(Date arrival) {
        this.arrival = arrival;
    }

    public Date getDeparture() {
        return departure;
    }

    public void setDeparture(Date departure) {
        this.departure = departure;
    }

    public Apartment getApartment() {
        return apartment;
    }

    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }

    public boolean isFirstDayFree() {
        return firstDayFree;
    }

    public void setFirstDayFree(boolean firstDayFree) {
        this.firstDayFree = firstDayFree;
    }

    public boolean isLastDayFree() {
        return lastDayFree;
    }

    public void setLastDayFree(boolean lastDayFree) {
        this.lastDayFree = lastDayFree;
    }

    public int getCurrentCountGuest() {
        return currentCountGuest;
    }

    public void setCurrentCountGuest(int currentCountGuest) {
        this.currentCountGuest = currentCountGuest;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setCanceled(boolean canceled) {
        isCanceled = canceled;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }
}
