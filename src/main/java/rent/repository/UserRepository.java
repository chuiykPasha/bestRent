package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rent.entities.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    @Query("Select u FROM User u join u.roles r where u.email = :email")
    User findByEmail(@Param("email")String email);
}
