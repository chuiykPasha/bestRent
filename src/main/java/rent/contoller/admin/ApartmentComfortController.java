package rent.contoller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import rent.entities.ApartmentComfort;
import rent.form.ApartmentComfortForm;
import rent.repository.ApartmentComfortRepository;

@Controller
@RequestMapping("/admin")
@Transactional
public class ApartmentComfortController {
    @Autowired
    private ApartmentComfortRepository apartmentComfortRepository;

    @GetMapping("/apartment-comfort")
    @Transactional(readOnly = true)
    public String index(Model model){
        model.addAttribute("apartmentComforts", apartmentComfortRepository.findAllActive());
        return "/admin/apartmentComfort/index";
    }

    @GetMapping("/apartment-comfort/create")
    public String save(ApartmentComfortForm apartmentComfortForm) {
        return "/admin/apartmentComfort/create";
    }

    @PostMapping("/apartment-comfort/create")
    public String save(ApartmentComfortForm apartmentComfortForm, BindingResult result) {
        if(result.hasErrors()) {
            return "/admin/apartmentComfort/create";
        }

        ApartmentComfort apartmentComfort = apartmentComfortRepository.findByNameAndIsActiveTrue(apartmentComfortForm.getName());

        if(apartmentComfort != null) {
            result.rejectValue("name", null, "Name already exists");
            return "/admin/apartmentComfort/create";
        }

        apartmentComfortRepository.save(new ApartmentComfort(apartmentComfortForm.getName()));
        return "redirect:/admin/apartment-comfort";
    }

    @GetMapping("/apartment-comfort/update/{apartmentComfort}")
    public String update(ApartmentComfort apartmentComfort, Model model) {
        model.addAttribute("apartmentComfortForm", new ApartmentComfortForm(apartmentComfort.getId(), apartmentComfort.getName()));
        return "/admin/apartmentComfort/update";
    }

    @PostMapping("/apartment-comfort/update")
    public String update(ApartmentComfortForm apartmentComfortForm, BindingResult result) {
        if(result.hasErrors()) {
            return "/admin/apartmentComfort/update";
        }

        ApartmentComfort apartmentComfort = apartmentComfortRepository.findByNameAndIsActiveTrue(apartmentComfortForm.getName());

        if(apartmentComfort != null) {
            result.rejectValue("name", null, "Name already exists");
            return "/admin/apartmentComfort/update";
        }

        apartmentComfort = apartmentComfortRepository.getOne(apartmentComfortForm.getId());
        apartmentComfort.setName(apartmentComfortForm.getName());
        return "redirect:/admin/apartment-comfort";
    }

    @GetMapping("/apartment-comfort/delete/{apartmentComfort}")
    public String delete(ApartmentComfort apartmentComfort, Model model) {
        model.addAttribute("apartmentComfortForm", new ApartmentComfortForm(apartmentComfort.getId(), apartmentComfort.getName()));
        return "/admin/apartmentComfort/confirmDelete";
    }

    @PostMapping("/apartment-comfort/delete")
    public String delete(ApartmentComfortForm apartmentComfortForm, Model model) {
        if(apartmentComfortForm.getId() != null) {
            ApartmentComfort find = apartmentComfortRepository.findById(apartmentComfortForm.getId()).get();

            if (!find.getApartments().isEmpty()) {
                model.addAttribute("error", "You can't delete this because this value is used in other advertisements");
                return "/admin/apartmentComfort/confirmDelete";
            }

            find.setActive(false);
        }

        return "redirect:/admin/apartment-comfort";
    }
}
