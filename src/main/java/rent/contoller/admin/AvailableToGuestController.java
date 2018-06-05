package rent.contoller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import rent.entities.AvailableToGuest;
import rent.form.AvailableToGuestForm;
import rent.repository.AvailableToGuestRepository;

import javax.validation.Valid;

@Controller
@RequestMapping("/admin")
public class AvailableToGuestController {
    @Autowired
    private AvailableToGuestRepository availableToGuestRepository;

    @GetMapping("/available-to-guest")
    public String index() {
        return "/admin/availableToGuest/index";
    }

    @GetMapping("/create/available-to-guest")
    public String save(AvailableToGuestForm availableToGuestForm) {
        return "/admin/availableToGuest/create";
    }

    @PostMapping("/create/available-to-guest")
    public String save(@Valid AvailableToGuestForm availableToGuestForm, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return "/admin/availableToGuest/create";
        }

        AvailableToGuest availableToGuest = availableToGuestRepository.findByName(availableToGuestForm.getName());

        if(availableToGuest != null) {
            return "/admin/availableToGuest/create";
        }

        availableToGuestRepository.save(new AvailableToGuest(availableToGuestForm.getName()));
        return "redirect:/admin/available-to-guest";
    }
}
