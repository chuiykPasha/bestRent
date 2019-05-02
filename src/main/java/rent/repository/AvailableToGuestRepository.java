package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import rent.entities.ApartmentComfort;
import rent.entities.AvailableToGuest;

import java.util.List;

@Transactional
public interface AvailableToGuestRepository extends JpaRepository<AvailableToGuest, Integer> {
    AvailableToGuest findByNameAndIsActiveTrue(String name);
    List<AvailableToGuest> findByIsActiveTrue();
    @Query("select atg from AvailableToGuest atg left join fetch atg.apartments a left join fetch a.typeOfHouse left join fetch a.availableToGuest left join fetch a.user where atg.isActive = true")
    List<AvailableToGuest> findAllActive();
}
