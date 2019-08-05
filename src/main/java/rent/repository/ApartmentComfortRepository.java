package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import rent.entities.ApartmentComfort;

import java.util.List;

@Transactional
public interface ApartmentComfortRepository extends JpaRepository<ApartmentComfort, Integer> {
    @Query("select (count(ac) > 0) from ApartmentComfort ac where ac.name = (:name) and ac.isActive = true")
    boolean isExistsByName(@Param("name") String name);
    @Query("select distinct ac from ApartmentComfort ac left join fetch ac.apartments a left join fetch a.typeOfHouse left join fetch a.availableToGuest left join fetch a.user where ac.isActive = true")
    List<ApartmentComfort> getAll();
}
