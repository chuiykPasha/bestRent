package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rent.entities.TypeOfHouse;

public interface TypeOfHouseRepository extends JpaRepository<TypeOfHouse, Integer> {
    TypeOfHouse findByName(String name);
}
