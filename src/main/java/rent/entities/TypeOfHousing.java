package rent.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "TypeOfHousing")
public class TypeOfHousing implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String name;

    public TypeOfHousing() {}

    public TypeOfHousing(Integer id, String name) {
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
