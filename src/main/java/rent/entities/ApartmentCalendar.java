package rent.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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

    @ManyToOne
    @JoinColumn(name = "apartmentId", nullable = false)
    private Apartment apartment;



    public ApartmentCalendar() {}

    public ApartmentCalendar(Date arrival, Date departure, Apartment apartment) {
        this.arrival = arrival;
        this.departure = departure;
        this.apartment = apartment;
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
}
