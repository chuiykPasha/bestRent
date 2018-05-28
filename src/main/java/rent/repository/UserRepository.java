package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rent.entities.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username);
}
