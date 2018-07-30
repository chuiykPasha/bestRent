package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rent.entities.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    PasswordResetToken findByToken(String token);
}
