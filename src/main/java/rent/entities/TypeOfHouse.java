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
@Table(name = "TypeOfHouse")
public class TypeOfHouse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private boolean isActive;

    @OneToMany(mappedBy = "typeOfHouse", fetch = FetchType.LAZY)
    private Set<Apartment> apartments = new HashSet<>();

    public TypeOfHouse(String name) {
        this.name = name;
        this.isActive = true;
    }
}
