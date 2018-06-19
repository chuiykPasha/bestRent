package rent.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import rent.form.ApartmentImagesForm;
import rent.form.ApartmentInfoForm;
import rent.form.ApartmentLocationForm;
import rent.repository.ApartmentComfortRepository;
import rent.repository.AvailableToGuestRepository;
import rent.repository.TypeOfHouseRepository;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Controller
@SessionAttributes(types = {ApartmentInfoForm.class, ApartmentLocationForm.class})
public class ApartmentController {
    @Autowired
    private TypeOfHouseRepository typeOfHouseRepository;
    @Autowired
    private AvailableToGuestRepository availableToGuestRepository;
    @Autowired
    private ApartmentComfortRepository apartmentComfortRepository;

    @GetMapping("/")
    public String main() {
        return "index";
    }

    @GetMapping("/apartment-create-step-one")
    public String fillApartmentInfo(Model model){
        ApartmentInfoForm apartmentInfoForm = new ApartmentInfoForm();
        apartmentInfoForm.setComforts(apartmentComfortRepository.findAll());
        apartmentInfoForm.setTypeOfHouses(typeOfHouseRepository.findAll());
        apartmentInfoForm.setAvailableToGuests(availableToGuestRepository.findAll());
        model.addAttribute("apartmentInfoForm", apartmentInfoForm);

        return "/apartment/createStepOne";
    }

    @PostMapping("/apartment-create-step-one")
    public String moveStepTwo(@Valid ApartmentInfoForm apartmentInfoForm, BindingResult result) {
        if(result.hasErrors()) {
            return "/apartment/createStepOne";
        }

        return "redirect:/apartment-create-step-two";
    }

    @GetMapping("/apartment-create-step-two")
    public String fillApartmentLocation(ApartmentLocationForm apartmentLocationForm) {
        return "/apartment/createStepTwo";
    }

    @PostMapping("/apartment-create-step-two")
    public String moveStepThree(@Valid @ModelAttribute ApartmentLocationForm apartmentLocationForm, BindingResult result) {
        if(result.hasErrors()) {
            return "/apartment/createStepTwo";
        }

        return "redirect:/apartment-create-step-three";
    }

    @GetMapping("/apartment-create-step-three")
    public String fillApartmentPhoto(ApartmentImagesForm apartmentImagesForm) {
        return "/apartment/createStepThree";
    }

    @PostMapping("/apartment-create-step-three")
    public String saveApartmentAdvertisement(@Valid ApartmentImagesForm apartmentImagesForm, BindingResult result, SessionStatus sessionStatus) {
        if(result.hasErrors()) {
            return "/apartment/createStepThree";
        }

        try {
            if(apartmentImagesForm.getImages().get(0).getBytes().length == 0) {
                result.rejectValue("images", null, "Need minimum one image");
                return "/apartment/createStepThree";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        sessionStatus.setComplete();

        return "redirect:/";
    }



    @GetMapping("/test")
    public String test(ApartmentInfoForm apartmentInfoForm, ApartmentLocationForm apartmentLocationForm) {
        //System.out.println(apartmentInfoForm.getPrice().setScale(2, BigDecimal.ROUND_HALF_DOWN) + " PRICE");
        //System.out.println("SIZE " + apartmentInfoForm.getSelectedComforts().size());
        System.out.println(apartmentInfoForm.getMaxNumberOfGuests() + " TEST");
        System.out.println("SIZE " + apartmentInfoForm.getSelectedComforts().size());
        System.out.println("LOCATION " + apartmentLocationForm.getLocation());
        return null;
    }
}
