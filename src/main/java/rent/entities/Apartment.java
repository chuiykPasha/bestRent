package rent.entities;

import javax.persistence.*;
import java.io.Serializable;

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

    public Apartment() {}

    public Apartment(String description, String location, float price, int maxNumberOfGuests) {
        this.description = description;
        this.location = location;
        this.price = price;
        this.maxNumberOfGuests = maxNumberOfGuests;
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
}
