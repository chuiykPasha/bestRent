package rent.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ApartmentComfort")
public class ApartmentComfort implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String name;

    public ApartmentComfort() {}

    public ApartmentComfort(String name) {
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
