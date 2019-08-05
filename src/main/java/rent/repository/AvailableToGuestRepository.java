package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import rent.entities.ApartmentComfort;
import rent.entities.AvailableToGuest;

import java.util.List;

@Transactional
public interface AvailableToGuestRepository extends JpaRepository<AvailableToGuest, Integer> {
    @Query("select (count(a) > 0) from AvailableToGuest  a where a.name = (:name) and a.isActive = true")
    boolean isExistsByName(@Param("name") String name);
    AvailableToGuest getByNameAndIsActiveTrue(String name);
    @Query("select distinct atg from AvailableToGuest atg left join fetch atg.apartments a left join fetch a.typeOfHouse left join fetch a.user where atg.isActive = true")
    List<AvailableToGuest> getAll();
}
