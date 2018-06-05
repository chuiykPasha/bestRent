package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rent.entities.AvailableToGuest;

public interface AvailableToGuestRepository extends JpaRepository<AvailableToGuest, Integer> {
    AvailableToGuest findByName(String name);
}
