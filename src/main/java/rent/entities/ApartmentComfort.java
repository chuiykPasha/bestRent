package rent.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ApartmentComfort")
public class ApartmentComfort {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private boolean isActive;

    @JsonIgnore
    @ManyToMany(mappedBy = "apartmentComforts", fetch = FetchType.LAZY)
    private Set<Apartment> apartments = new HashSet<>();

    public ApartmentComfort(String name) {
        this.name = name;
        this.isActive = true;
    }
}
