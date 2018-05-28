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
    public String addUser(User user) {
        System.out.println("TYT");
        User find = userRepository.findByUsername(user.getUsername());

        if(find != null) {
            return "/register";
        }

        user.setActive(true);
        user.setRoles(Collections.singleton(Role.USER));
        System.out.println(user.isActive());
        userRepository.save(user);
        return "redirect:/login";
    }
}
