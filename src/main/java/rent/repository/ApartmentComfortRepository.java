package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rent.entities.ApartmentComfort;

import java.util.List;

public interface ApartmentComfortRepository extends JpaRepository<ApartmentComfort, Integer> {
    ApartmentComfort findByNameAndIsActiveTrue(String name);
    List<ApartmentComfort> findByIsActiveTrue();
}
