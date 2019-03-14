package rent.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ApartmentImage")
public class ApartmentImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String pathPhoto;

    @Column(nullable = false)
    private String linkPhoto;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "apartmentId", nullable = false)
    private Apartment apartment;

    private double sizeInBites;

    public ApartmentImage(String pathPhoto, String linkPhoto, Apartment apartment, double sizeInBites) {
        this.pathPhoto = pathPhoto;
        this.linkPhoto = linkPhoto;
        this.apartment = apartment;
        this.sizeInBites = sizeInBites;
    }
}
