package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rent.entities.TypeOfHouse;

import java.util.List;

public interface TypeOfHouseRepository extends JpaRepository<TypeOfHouse, Integer> {
    TypeOfHouse findByNameAndIsActiveTrue(String name);
    List<TypeOfHouse> findByIsActiveTrue();
}
