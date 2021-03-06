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
    public String fillApartmentInfo(ApartmentInfoForm apartmentInfoForm, Model model){
        apartmentInfoForm.setComforts(apartmentComfortRepository.getAll());
        apartmentInfoForm.setTypeOfHouses(typeOfHouseRepository.getAll());
        apartmentInfoForm.setAvailableToGuests(availableToGuestRepository.getAll());
        model.addAttribute("privateRoomId", findIdFromAvailableToGuests(apartmentInfoForm.getAvailableToGuests(),"Private room"));
        return "/apartment/createStepOne";
    }

    @PostMapping("/apartment-create-step-one")
    public String moveStepTwo(@Valid ApartmentInfoForm apartmentInfoForm, BindingResult result, Model model) {
        if(result.hasErrors()) {
            return "/apartment/createStepOne";
        }

        final int privateRoomId = availableToGuestRepository.getByNameAndIsActiveTrue("Private room").getId();
        if(isNumberOfRoomsMoreThanGuests(apartmentInfoForm.getNumberOfRooms(), apartmentInfoForm.getMaxNumberOfGuests())){
            result.rejectValue("maxNumberOfGuests", null, "Check the max number of guests.Number of rooms more than guests.");
            model.addAttribute("privateRoomId", privateRoomId);
            return "/apartment/createStepOne";
        }

        if(apartmentInfoForm.getAvailableToGuestId() == privateRoomId){
            final int countGuestsInRooms = apartmentInfoForm.getGuestsInRoom().stream().mapToInt(i -> i).sum();

            if(isMaxNumberOfGuestsNotEqualsCountGuestsInRooms(countGuestsInRooms, apartmentInfoForm.getMaxNumberOfGuests())){
                result.rejectValue("guestsInRoom", null, "Max number of guests and guests in the rooms not equals.");
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
        Set<ApartmentComfort> selectedComforts = getComfortsToSave(apartmentInfoForm.getSelectedComforts());

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

    private Set<ApartmentComfort> getComfortsToSave(List<Integer> selectedComforts){
        Set<ApartmentComfort> result = new HashSet<>();
        List<ApartmentComfort> apartmentComforts = apartmentComfortRepository.getAll();

        for(int selected : selectedComforts) {
            result.add(apartmentComforts.stream()
                    .filter(i -> i.getId() == selected)
                    .findAny()
                    .get());
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
        if(apartmentInfoForm.getAvailableToGuestId().equals(availableToGuestRepository.getByNameAndIsActiveTrue("Private room").getId())){
            List<Room> rooms = new ArrayList<>();

            for(int guestsInRoom: apartmentInfoForm.getGuestsInRoom()){
                rooms.add(new Room(guestsInRoom, new Apartment(apartmentId)));
            }

            roomRepository.saveAll(rooms);
        }
    }

    private int findIdFromAvailableToGuests(List<AvailableToGuest> availableToGuests, final String find){
        return availableToGuests.stream()
                .filter(i -> i.getName().equals(find))
                .findAny()
                .get()
                .getId();
    }

    private boolean isNumberOfRoomsMoreThanGuests(int numberOfRooms, int maxNumberOfGuests){
        return numberOfRooms > maxNumberOfGuests;
    }

    private boolean isMaxNumberOfGuestsNotEqualsCountGuestsInRooms(int countGuestsInRooms, int maxNumberOfGuests){
        return countGuestsInRooms != maxNumberOfGuests;
    }
}
