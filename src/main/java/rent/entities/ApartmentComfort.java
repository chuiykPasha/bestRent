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

    @ManyToMany(mappedBy = "apartmentComforts")
    private Set<Apartment> apartments = new HashSet<>();

    public ApartmentComfort() {}

    public ApartmentComfort(String name) {
        this.name = name;
    }

    public ApartmentComfort(Integer id, String name) {
        this.id = id;
        this.name = name;
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
}
