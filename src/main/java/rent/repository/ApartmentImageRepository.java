package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rent.entities.ApartmentImage;

import java.util.List;

public interface ApartmentImageRepository extends JpaRepository<ApartmentImage, Integer> {
    List<ApartmentImage> findByApartmentId(int apartmentId);
}
