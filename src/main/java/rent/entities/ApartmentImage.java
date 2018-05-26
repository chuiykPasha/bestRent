package rent.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ApartmentImage")
public class ApartmentImage implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer Id;
    @Column(nullable = false)
    private String path;

    @ManyToOne
    @JoinColumn(name = "apartmentId", nullable = false)
    private Apartment apartment;

    public ApartmentImage() {}

    public ApartmentImage(String path, Apartment apartment) {
        this.path = path;
        this.apartment = apartment;
    }

    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        this.Id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Apartment getApartment() {
        return apartment;
    }

    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }
}
