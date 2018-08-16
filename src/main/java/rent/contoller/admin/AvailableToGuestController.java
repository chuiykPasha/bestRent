package rent.contoller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rent.entities.AvailableToGuest;
import rent.form.AvailableToGuestForm;
import rent.repository.AvailableToGuestRepository;
import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AvailableToGuestController {
    @Autowired
    private AvailableToGuestRepository availableToGuestRepository;

    @GetMapping("/available-to-guest")
    public String index(Model model) {
        model.addAttribute("availableToGuests", availableToGuestRepository.findAll());
        return "/admin/availableToGuest/index";
    }

    @GetMapping("/available-to-guest/create")
    public String save(AvailableToGuestForm availableToGuestForm) {
        return "/admin/availableToGuest/create";
    }

    @PostMapping("/available-to-guest/create")
    public String save(@Valid AvailableToGuestForm availableToGuestForm, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            return "/admin/availableToGuest/create";
        }

        AvailableToGuest availableToGuest = availableToGuestRepository.findByNameAndIsActiveTrue(availableToGuestForm.getName());

        if(availableToGuest != null) {
            bindingResult.rejectValue("name", null, "Name already exists");
            return "/admin/availableToGuest/create";
        }

        availableToGuestRepository.save(new AvailableToGuest(availableToGuestForm.getName()));
        return "redirect:/admin/available-to-guest";
    }

    @GetMapping("/available-to-guest/update/{availableToGuest}")
    public String update(AvailableToGuest availableToGuest, Model model) {
        model.addAttribute("availableToGuestForm", new AvailableToGuestForm(availableToGuest.getId(), availableToGuest.getName()));
        return "/admin/availableToGuest/update";
    }

    @PostMapping("/available-to-guest/update")
    public String update(@Valid AvailableToGuestForm availableToGuestForm, BindingResult bindingResult, RedirectAttributes attr) {
        if(bindingResult.hasErrors()) {
            return "/admin/availableToGuest/update";
        }

        AvailableToGuest update = availableToGuestRepository.findByNameAndIsActiveTrue(availableToGuestForm.getName());

        if(update != null) {
            bindingResult.rejectValue("name", null, "Name already exists");
            return "/admin/availableToGuest/update";
        }

        update = new AvailableToGuest(availableToGuestForm.getId(), availableToGuestForm.getName());
        availableToGuestRepository.save(update);
        return "redirect:/admin/available-to-guest";
    }

    @GetMapping("/available-to-guest/delete/{availableToGuest}")
    public String delete(AvailableToGuest availableToGuest, Model model) {
        model.addAttribute("availableToGuestForm", new AvailableToGuestForm(availableToGuest.getId(), availableToGuest.getName()));
        return "/admin/availableToGuest/confirmDelete";
    }

    @PostMapping("/available-to-guest/delete")
    public String delete(AvailableToGuestForm availableToGuestForm, Model model) {
        if(availableToGuestForm.getId() != null) {
            AvailableToGuest find = availableToGuestRepository.getOne(availableToGuestForm.getId());

            if(!find.getApartments().isEmpty()){
                model.addAttribute("error", "You can't delete this because this value is used in other advertisements");
                return "/admin/availableToGuest/confirmDelete";
            }
        }

        return "redirect:/admin/available-to-guest";
    }
}
