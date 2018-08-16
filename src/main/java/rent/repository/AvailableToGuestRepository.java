package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rent.entities.AvailableToGuest;

import java.util.List;

public interface AvailableToGuestRepository extends JpaRepository<AvailableToGuest, Integer> {
    AvailableToGuest findByNameAndIsActiveTrue(String name);
    List<AvailableToGuest> findByIsActiveTrue();
}
