package rent.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import rent.config.BeanConfig;
import rent.entities.Role;
import rent.entities.User;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(BeanConfig.class)
public class UserRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private UserRepository userRepository;
    private final String EMAIL = "test111@gmail.com";

    @Test
    public void findByEmail() {
        entityManager.persist(getUser());

        User find = userRepository.findByEmail(EMAIL);

        assertNotNull(find);
        assertEquals(EMAIL, find.getEmail());
    }

    @Test
    public void save(){
        User save = entityManager.persist(getUser());
        assertNotNull(save);
        assertNotNull(save.getId());

    }

    private User getUser(){
        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        return new User(EMAIL, "vasya", "pupkin", "111", roles);
    }
}