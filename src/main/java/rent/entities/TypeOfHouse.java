package rent.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TypeOfHouse")
public class TypeOfHouse implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String name;
    @Column
    private boolean isActive;
    @OneToMany(mappedBy = "typeOfHouse", fetch = FetchType.LAZY)
    private Set<Apartment> apartments = new HashSet<>();

    public TypeOfHouse() {}

    public TypeOfHouse(String name) {
        this.name = name;
        this.isActive = true;
    }

    public TypeOfHouse(Integer id, String name) {
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
