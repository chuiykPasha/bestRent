package rent.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import rent.entities.*;
import rent.form.ApartmentImagesForm;
import rent.form.ApartmentInfoForm;
import rent.form.ApartmentLocationForm;
import rent.repository.*;
import rent.service.UploadImageService;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@SessionAttributes(types = {ApartmentInfoForm.class, ApartmentLocationForm.class})
public class AddApartmentController {
    @Autowired
    private ApartmentComfortRepository apartmentComfortRepository;
    @Autowired
    private TypeOfHouseRepository typeOfHouseRepository;
    @Autowired
    private AvailableToGuestRepository availableToGuestRepository;
    @Autowired
    private ApartmentRepository apartmentRepository;
    @Autowired
    private UploadImageService uploadImageService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    protected RoomRepository roomRepository;

    @GetMapping("/apartment-create-step-one")
    public String fillApartmentInfo(Model model){
        ApartmentInfoForm apartmentInfoForm = new ApartmentInfoForm();
        apartmentInfoForm.setComforts(apartmentComfortRepository.getAll());
        apartmentInfoForm.setTypeOfHouses(typeOfHouseRepository.findByIsActiveTrue());
        apartmentInfoForm.setAvailableToGuests(availableToGuestRepository.findByIsActiveTrue());
        model.addAttribute("apartmentInfoForm", apartmentInfoForm);
        model.addAttribute("privateRoomId", availableToGuestRepository.findByNameAndIsActiveTrue("Private room").getId());

        return "/apartment/createStepOne";
    }

    @PostMapping("/apartment-create-step-one")
    public String moveStepTwo(@Valid ApartmentInfoForm apartmentInfoForm, BindingResult result, Model model) {
        if(result.hasErrors()) {
            return "/apartment/createStepOne";
        }

        int privateRoomId = availableToGuestRepository.findByNameAndIsActiveTrue("Private room").getId();

        if(apartmentInfoForm.getNumberOfRooms() > apartmentInfoForm.getMaxNumberOfGuests()){
            result.rejectValue("maxNumberOfGuests", null, "Check the maximum number of guests");
            model.addAttribute("privateRoomId", privateRoomId);
            return "/apartment/createStepOne";
        }

        if(apartmentInfoForm.getAvailableToGuestId() == privateRoomId){
            int countGuestsInRooms = apartmentInfoForm.getGuestsInRoom().stream().mapToInt(i -> i).sum();

            if(countGuestsInRooms != apartmentInfoForm.getMaxNumberOfGuests()){
                result.rejectValue("guestsInRoom", null, "Maximum number of guests and guests in the room.");
                model.addAttribute("privateRoomId", privateRoomId);
                return "/apartment/createStepOne";
            }
        }

        return "redirect:/apartment-create-step-two";
    }

    @GetMapping("/apartment-create-step-two")
    public String fillApartmentLocation(ApartmentLocationForm apartmentLocationForm) {
        return "/apartment/createStepTwo";
    }

    @PostMapping("/apartment-create-step-two")
    public String moveStepThree(@Valid @ModelAttribute ApartmentLocationForm apartmentLocationForm, BindingResult result) {
        /*if(result.hasErrors()) {
            return "/apartment/createStepTwo";
        }
        */

        return "redirect:/apartment-create-step-three";
    }

    @GetMapping("/apartment-create-step-three")
    public String fillApartmentPhoto(ApartmentImagesForm apartmentImagesForm) {
        return "/apartment/createStepThree";
    }

    @PostMapping("/apartment-create-step-three")
    public String saveApartmentAdvertisement(@Valid ApartmentImagesForm apartmentImagesForm,
                                             BindingResult result,
                                             ApartmentInfoForm apartmentInfoForm,
                                             ApartmentLocationForm apartmentLocationForm,
                                             SessionStatus sessionStatus,
                                             @AuthenticationPrincipal User user) throws MaxUploadSizeExceededException {
        if(result.hasErrors()) {
            return "/apartment/createStepThree";
        }

        final UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Set<ApartmentComfort> selectedComforts = getSelectedComforts(apartmentInfoForm.getSelectedComforts());

        Apartment apartment = new Apartment(apartmentInfoForm.getDescription(), apartmentLocationForm.getLocation(), apartmentInfoForm.getPrice().floatValue(),
                apartmentInfoForm.getMaxNumberOfGuests(),
                typeOfHouseRepository.getOne(apartmentInfoForm.getTypeOfHouseId()),
                availableToGuestRepository.getOne(apartmentInfoForm.getAvailableToGuestId()), selectedComforts, apartmentInfoForm.getTitle(), user,
                apartmentLocationForm.getLongitude(), apartmentLocationForm.getLatitude(), apartmentInfoForm.getNumberOfRooms());

        final int apartmentId = apartmentRepository.save(apartment).getId();

        uploadImageService.uploadApartmentImages(apartmentImagesForm.getImages(), userDetails.getUsername(), apartmentId, apartmentImagesForm.getImagesSize());
        sessionStatus.setComplete();
        updateUserRole(user);
        waitForUploadFirstImg();
        saveApartmentRooms(apartmentInfoForm, apartmentId);
        return "redirect:/";
    }

    private Set<ApartmentComfort> getSelectedComforts(List<Integer> selectedComforts){
        Set<ApartmentComfort> result = new HashSet<>();

        for(int selected : selectedComforts) {
            result.add(apartmentComfortRepository.getOne(selected));
        }

        return result;
    }

    private void waitForUploadFirstImg(){
        while (true) {
            if(UploadImageService.firstImageUploaded) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void updateUserRole(User user){
        if(!user.getRoles().contains(Role.LANDLORD)) {
            user.getRoles().add(Role.LANDLORD);
            userRepository.save(user);
        }

        addNewUserRoleInSession(Role.LANDLORD);
    }

    private void addNewUserRoleInSession(GrantedAuthority authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<GrantedAuthority> updatedAuthorities = new ArrayList<>(auth.getAuthorities());
        updatedAuthorities.add(authority);
        Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), updatedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    private void saveApartmentRooms(ApartmentInfoForm apartmentInfoForm, int apartmentId){
        if(apartmentInfoForm.getAvailableToGuestId().equals(availableToGuestRepository.findByNameAndIsActiveTrue("Private room").getId())){
            List<Room> rooms = new ArrayList<>();

            for(int guestsInRoom: apartmentInfoForm.getGuestsInRoom()){
                rooms.add(new Room(guestsInRoom, new Apartment(apartmentId)));
            }

            roomRepository.saveAll(rooms);
        }
    }
}
