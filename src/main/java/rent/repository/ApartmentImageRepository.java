package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rent.entities.ApartmentImage;

public interface ApartmentImageRepository extends JpaRepository<ApartmentImage, Integer> {
}
