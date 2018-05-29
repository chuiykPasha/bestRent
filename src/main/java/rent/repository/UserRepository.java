package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rent.entities.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findByEmail(String email);
}
