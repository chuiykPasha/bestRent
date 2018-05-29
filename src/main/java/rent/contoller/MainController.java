package rent.contoller;

import org.hibernate.mapping.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import rent.entities.Role;
import rent.entities.User;
import rent.repository.UserRepository;

import java.util.Collections;

@Controller
public class MainController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String main() {
        return "main";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String register(User user) {
        User find = userRepository.findByEmail(user.getEmail());

        if(find != null) {
            return "/register";
        }

        user.setRoles(Collections.singleton(Role.USER));
        userRepository.save(user);
        return "redirect:/login";
    }
}
