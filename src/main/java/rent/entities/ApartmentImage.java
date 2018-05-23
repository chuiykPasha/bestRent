package rent.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "ApartmentImage")
public class ApartmentImage implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String path;

    public ApartmentImage() {}

    public ApartmentImage(String path) {
        this.path = path;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
