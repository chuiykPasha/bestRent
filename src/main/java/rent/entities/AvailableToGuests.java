package rent.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "AvailableToGuest")
public class AvailableToGuests implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String name;

    public AvailableToGuests() {}

    public AvailableToGuests(String name) {
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
