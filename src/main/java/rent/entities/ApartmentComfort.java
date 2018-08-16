package rent.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ApartmentComfort")
public class ApartmentComfort implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String name;
    @Column
    private boolean isActive;

    @ManyToMany(mappedBy = "apartmentComforts", fetch = FetchType.LAZY)
    private Set<Apartment> apartments;

    public ApartmentComfort() {}

    public ApartmentComfort(String name) {
        this.name = name;
    }

    public ApartmentComfort(int id) {
        this.id = id;
    }

    public ApartmentComfort(Integer id, String name) {
        this.id = id;
        this.name = name;
        this.isActive = true;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Apartment> getApartments() {
        return apartments;
    }

    public void setApartments(Set<Apartment> apartments) {
        this.apartments = apartments;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
