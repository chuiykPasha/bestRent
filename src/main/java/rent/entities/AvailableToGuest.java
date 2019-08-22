package rent.entities;

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
@Table(name = "AvailableToGuest")
public class AvailableToGuest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    private boolean isActive;

    @OneToMany(mappedBy = "availableToGuest", fetch = FetchType.LAZY)
    private Set<Apartment> apartments = new HashSet<>();

    public AvailableToGuest(String name) {
        this.name = name;
        this.isActive = true;
    }
}
