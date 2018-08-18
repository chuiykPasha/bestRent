package rent.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ApartmentImage")
public class ApartmentImage implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String pathPhoto;

    @Column(nullable = false)
    private String linkPhoto;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "apartmentId", nullable = false)
    private Apartment apartment;

    private double sizeInBites;

    public ApartmentImage() {}

    public ApartmentImage(String pathPhoto, String linkPhoto, Apartment apartment, double sizeInBites) {
        this.pathPhoto = pathPhoto;
        this.linkPhoto = linkPhoto;
        this.apartment = apartment;
        this.sizeInBites = sizeInBites;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPathPhoto() {
        return pathPhoto;
    }

    public void setPathPhoto(String pathPhoto) {
        this.pathPhoto = pathPhoto;
    }

    public String getLinkPhoto() {
        return linkPhoto;
    }

    public void setLinkPhoto(String linkPhoto) {
        this.linkPhoto = linkPhoto;
    }

    public Apartment getApartment() {
        return apartment;
    }

    public void setApartment(Apartment apartment) {
        this.apartment = apartment;
    }

    public double getSizeInBites() {
        return sizeInBites;
    }

    public void setSizeInBites(double sizeInBites) {
        this.sizeInBites = sizeInBites;
    }
}
