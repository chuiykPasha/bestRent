package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import rent.entities.ApartmentComfort;
import rent.entities.TypeOfHouse;

import java.util.List;

@Transactional
public interface TypeOfHouseRepository extends JpaRepository<TypeOfHouse, Integer> {
    @Query("select (count(t) > 0) from TypeOfHouse t where t.name = (:name) and t.isActive = true")
    boolean isExistsByName(@Param("name")String name);
    @Query("select distinct t from TypeOfHouse t left join fetch t.apartments a left join fetch a.availableToGuest left join fetch a.user where t.isActive = true")
    List<TypeOfHouse> getAll();
}
