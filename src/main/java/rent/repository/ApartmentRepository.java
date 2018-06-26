package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rent.entities.Apartment;

public interface ApartmentRepository extends JpaRepository<Apartment, Integer> {
}
