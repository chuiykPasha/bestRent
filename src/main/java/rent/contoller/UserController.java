package rent.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import rent.entities.Role;
import rent.entities.User;
import rent.form.AddAvatarForm;
import rent.form.ChangePasswordForm;
import rent.form.ChangeProfileForm;
import rent.form.RegisterForm;
import rent.repository.UserRepository;
import rent.service.UploadImageService;

import javax.validation.Valid;
import java.util.Collections;

@Controller
@Transactional
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UploadImageService uploadImageService;

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

    @GetMapping("/user-profile/{user}")
    public String showProfile(User user, Model model) {
        String avatar = user.getAvatarUrl() == null ? User.DEFAULT_AVATAR : user.getAvatarUrl();
        model.addAttribute("avatar", avatar);
        model.addAttribute("user", user);
        return "/userProfile/userProfile";
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
    public String changePassword(ChangePasswordForm changePasswordForm) {
        return "/userProfile/changePassword";
    }

    @PostMapping("/change-password")
    public String changePasswordSave(@AuthenticationPrincipal User user, @Valid @ModelAttribute ChangePasswordForm form, BindingResult result) {
        if(result.hasErrors()) {
            return "/userProfile/changePassword";
        }

        if(!passwordEncoder.matches(form.getOldPassword(), user.getPassword())) {
            result.rejectValue("oldPassword", null, "Old password wrong.");
            return "/userProfile/changePassword";
        }

        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        return "/userProfile/changeProfile";
    }

    @GetMapping("/user-photo")
    public String userPhoto(@AuthenticationPrincipal User user, Model model) {
        String avatarUrl = user.getAvatarUrl() != null ? user.getAvatarUrl() : User.DEFAULT_AVATAR;
        model.addAttribute("avatar", avatarUrl);

        return "/userProfile/userPhoto";
    }

    @PostMapping("/user-photo")
    public String userPhotoSave(@AuthenticationPrincipal User user, @Valid AddAvatarForm addAvatarForm, Model model){
        String avatarUrl =  uploadImageService.uploadAvatar(addAvatarForm.getAvatar(), user.getEmail());
        user.setAvatarUrl(avatarUrl);
        model.addAttribute("avatar", avatarUrl);
        return "/userProfile/userPhoto";
    }
}
