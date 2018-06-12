package rent.contoller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import rent.entities.TypeOfHouse;
import rent.form.TypeOfHouseForm;
import rent.repository.TypeOfHouseRepository;

import javax.validation.Valid;

@Controller
@RequestMapping("/admin")
public class TypeOfHouseController {
    @Autowired
    private TypeOfHouseRepository typeOfHouseRepository;

    @GetMapping("/type-of-house")
    public String index(Model model) {
        model.addAttribute("typeOfHouses", typeOfHouseRepository.findAll());
        return "/admin/typeOfHouse/index";
    }

    @GetMapping("/type-of-house/create")
    public String save(TypeOfHouseForm typeOfHouseForm) {
        return "/admin/typeOfHouse/create";
    }

    @PostMapping("/type-of-house/create")
    public String save(@Valid TypeOfHouseForm typeOfHouseForm, BindingResult result) {
        if(result.hasErrors()) {
            return "/admin/typeOfHouse/create";
        }

        TypeOfHouse typeOfHouse = typeOfHouseRepository.findByName(typeOfHouseForm.getName());

        if(typeOfHouse != null) {
            result.rejectValue("name", null, "Name already exists");
            return "/admin/typeOfHouse/create";
        }

        typeOfHouseRepository.save(new TypeOfHouse(typeOfHouseForm.getName()));
        return "redirect:/admin/type-of-house";
    }

    @GetMapping("/type-of-house/update/{typeOfHouse}")
    public String update(TypeOfHouse typeOfHous, Model model) {
        model.addAttribute("typeOfHouseForm", new TypeOfHouseForm(typeOfHous.getId(), typeOfHous.getName()));
        return "/admin/typeOfHouse/update";
    }

    @PostMapping("/type-of-house/update")
    public String update(@Valid TypeOfHouseForm typeOfHouseForm, BindingResult result) {
        if(result.hasErrors()) {
            return "/admin/typeOfHouse/update";
        }

        TypeOfHouse typeOfHouse = typeOfHouseRepository.findByName(typeOfHouseForm.getName());

        if(typeOfHouse != null) {
            result.rejectValue("name", null, "Name already exists");
            return "/admin/typeOFHouse/update";
        }

        typeOfHouse = new TypeOfHouse(typeOfHouseForm.getId(), typeOfHouseForm.getName());
        typeOfHouseRepository.save(typeOfHouse);
        return "redirect:/admin/type-of-house";
    }

    @GetMapping("/type-of-house/delete/{typeOfHouse}")
    public String delete(TypeOfHouse typeOfHouse, Model model) {
        model.addAttribute("typeOfHouseForm", new TypeOfHouseForm(typeOfHouse.getId(), typeOfHouse.getName()));
        return "/admin/typeOfHouse/confirmDelete";
    }

    @PostMapping("/type-of-house/delete")
    public String delete(TypeOfHouseForm typeOfHouseForm) {
        if(typeOfHouseForm.getId() != null) {
            typeOfHouseRepository.deleteById(typeOfHouseForm.getId());
        }

        return "redirect:/admin/type-of-house";
    }
}
