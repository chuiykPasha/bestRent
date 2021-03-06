package rent.contoller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional
public class AvailableToGuestController {
    @Autowired
    private AvailableToGuestRepository availableToGuestRepository;

    @GetMapping("/available-to-guest")
    @Transactional(readOnly = true)
    public String index(Model model) {
        model.addAttribute("availableToGuests", availableToGuestRepository.getAll());
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

        if(availableToGuestRepository.isExistsByName(availableToGuestForm.getName())) {
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

        if(availableToGuestRepository.isExistsByName(availableToGuestForm.getName())) {
            bindingResult.rejectValue("name", null, "Name already exists");
            return "/admin/availableToGuest/update";
        }

        AvailableToGuest update = availableToGuestRepository.getOne(availableToGuestForm.getId());
        update.setName(availableToGuestForm.getName());
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
            AvailableToGuest find = availableToGuestRepository.findById(availableToGuestForm.getId()).get();

            if(!find.getApartments().isEmpty()){
                model.addAttribute("error", "You can't delete this because this value is used in other advertisements");
                return "/admin/availableToGuest/confirmDelete";
            }

            find.setActive(false);
        }

        return "redirect:/admin/available-to-guest";
    }
}
