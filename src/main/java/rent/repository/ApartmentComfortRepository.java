package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rent.entities.ApartmentComfort;

public interface ApartmentComfortRepository extends JpaRepository<ApartmentComfort, Integer> {
    ApartmentComfort findByName(String name);
}
