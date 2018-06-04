package rent.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import rent.entities.Role;
import rent.entities.User;
import rent.form.RegisterForm;
import rent.repository.UserRepository;
import javax.validation.Valid;
import java.util.Collections;

@Controller
public class MainController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String main() {
        return "main";
    }

    @GetMapping("/register")
    public String register(RegisterForm registerForm) {
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid RegisterForm registerForm, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return "register";
        }

        User find = userRepository.findByEmail(registerForm.getEmail());

        if(find != null) {
            return "register";
        }

        User user = new User(registerForm.getEmail(),null, null,
                passwordEncoder.encode(registerForm.getPassword()),
                Collections.singleton(Role.USER));
        userRepository.save(user);
        return "redirect:/login";
    }
}
