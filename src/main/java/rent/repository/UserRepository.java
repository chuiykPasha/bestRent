package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rent.entities.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("Select u FROM User u join u.roles r")
    User findByEmail(String email);
}
