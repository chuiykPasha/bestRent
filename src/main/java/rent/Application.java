package rent;

import org.hibernate.Hibernate;
import org.hibernate.annotations.NaturalId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import rent.dto.MailDto;
import rent.entities.*;
import rent.repository.ApartmentCalendarRepository;
import rent.repository.ApartmentRepository;
import rent.repository.UserRepository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@SpringBootApplication
public class Application implements CommandLineRunner{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        User user = userRepository.findByEmail("pasha@gmail.com");

        if(user == null) {
            Set<Role> roles = new HashSet<>();
            roles.add(Role.USER);
            roles.add(Role.ADMIN);

            user = new User("pasha@gmail.com", "pasha", "bubkin",
                    passwordEncoder.encode("123321"),
                    roles);
            userRepository.save(user);
        }
    }
}
