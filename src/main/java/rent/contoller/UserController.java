package rent.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import rent.entities.Role;
import rent.entities.User;
import rent.form.ChangeProfileForm;
import rent.form.RegisterForm;
import rent.repository.UserRepository;

import javax.validation.Valid;
import java.util.Collections;

@Controller
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
            bindingResult.rejectValue("email", null, "Email already exists");
            return "register";
        }

        User user = new User(registerForm.getEmail(), registerForm.getName(), registerForm.getSurName(),
                passwordEncoder.encode(registerForm.getPassword()),
                Collections.singleton(Role.USER));
        userRepository.save(user);
        return "redirect:/login";
    }

    @GetMapping("/user-profile")
    public String showProfile() {
        return "/userProfile/index";
    }

    @GetMapping("/change-profile")
    public String changeProfile(@AuthenticationPrincipal User user, Model model) {
        ChangeProfileForm changeProfileForm = new ChangeProfileForm(user.getEmail(), user.getName(), user.getSurName());
        model.addAttribute("changeProfileForm", changeProfileForm);
        return "/userProfile/changeProfile";
    }

    @PostMapping("/change-profile")
    public String changeProfileSave(@Valid @ModelAttribute ChangeProfileForm form, BindingResult result, @AuthenticationPrincipal User user) {
        if(result.hasErrors()) {
            return "/userProfile/changeProfile";
        }

        if(!form.getEmail().equals(user.getEmail()) && userRepository.findByEmail(form.getEmail()) != null) {
            result.rejectValue("email", null, "Email already exists");
            return "/userProfile/changeProfile";
        }

        user.setEmail(form.getEmail());
        user.setName(form.getName());
        user.setSurName(form.getSurName());
        userRepository.save(user);
        return "/userProfile/changeProfile";
    }

    @GetMapping("/change-password")
    public String changePassword(@AuthenticationPrincipal User user, Model model) {
        return "/userProfile/changePassword";
    }

    @PostMapping("/change-password")
    public String changePasswordSave() {
        return null;
    }
}
