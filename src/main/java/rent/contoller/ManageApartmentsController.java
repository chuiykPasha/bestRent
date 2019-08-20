package rent.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import rent.dto.MailDto;
import rent.entities.*;
import rent.form.ChangeApartmentImagesForm;
import rent.form.ChangeApartmentInfoForm;
import rent.form.ChangeApartmentLocationForm;
import rent.form.DeleteApartmentForm;
import rent.repository.ApartmentCalendarRepository;
import rent.repository.ApartmentComfortRepository;
import rent.repository.ApartmentRepository;
import rent.repository.RoomRepository;
import rent.service.EmailService;
import rent.service.UploadImageService;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Controller
@Transactional
public class ManageApartmentsController {
    @Autowired
    private ApartmentRepository apartmentRepository;
    @Autowired
    private ApartmentCalendarRepository apartmentCalendarRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private ApartmentComfortRepository apartmentComfortRepository;
    @Autowired
    private UploadImageService uploadImageService;

    @GetMapping("my-advertisements")
    public String showMyAdvertisements(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("apartments", apartmentRepository.getAdvertisementsByUserId(user.getId()));
        return "myAdvertisements";
    }

    @GetMapping("/change-apartment-location/{apartment}")
    public String changeApartmentLocation(Apartment apartment, ChangeApartmentLocationForm changeLocationForm, Model model){
        changeLocationForm.setLocation(apartment.getLocation());
        changeLocationForm.setLatitude(apartment.getLatitude());
        changeLocationForm.setLongitude(apartment.getLongitude());
        changeLocationForm.setApartmentId(apartment.getId());
        model.addAttribute("changeLocationForm", changeLocationForm);
        return "/apartment/changeLocation";
    }

    @PostMapping("/change-apartment-location")
    public String changeApartmentLocationSave(@Valid ChangeApartmentLocationForm changeLocationForm, BindingResult result){
        if(result.hasErrors()) {
            return "/apartment/changeLocation";
        }

        Apartment changeApartment = apartmentRepository.findById(changeLocationForm.getApartmentId()).get();

        if(hasBooking(changeApartment)){
            sendEmailsAboutChangedLocation(changeApartment, changeLocationForm.getLocation());
        }

        changeApartment.setLocation(changeLocationForm.getLocation());
        changeApartment.setLatitude(changeLocationForm.getLatitude());
        changeApartment.setLongitude(changeLocationForm.getLongitude());

        return "redirect:/my-advertisements";
    }

    @GetMapping("/change-apartment-images/{apartment}")
    public String changeApartmentImages(Apartment apartment, ChangeApartmentImagesForm changeApartmentImagesForm, Model model) {
        changeApartmentImagesForm.setApartmentId(apartment.getId());
        model.addAttribute("changeApartmentImagesForm", changeApartmentImagesForm);
        model.addAttribute("images", apartment.getImages());
        return "/apartment/changeApartmentImages";
    }

    @PostMapping("/change-apartment-images")
    public String changeApartmentImagesSave(@Valid ChangeApartmentImagesForm changeApartmentImagesForm, @AuthenticationPrincipal User user, Model model){
        if(changeApartmentImagesForm.getImages().isEmpty()){
            model.addAttribute("changeApartmentImagesForm", changeApartmentImagesForm);
            model.addAttribute("images", apartmentRepository.findById(changeApartmentImagesForm.getApartmentId()).get());

            return "/apartment/changeApartmentImages";
        }

        uploadImageService.changeApartmentImages(changeApartmentImagesForm.getImages(), user.getEmail() ,changeApartmentImagesForm.getApartmentId(), changeApartmentImagesForm.getImagesSize());
        return "redirect:/my-advertisements";
    }

    @GetMapping("/change-apartment-info/{apartment}")
    public String changeApartmentInfo(Apartment apartment, Model model){
        ChangeApartmentInfoForm changeApartmentInfoForm = new ChangeApartmentInfoForm(apartment.getId(), apartment.getDescription(), BigDecimal.valueOf(apartment.getPrice()),
                apartment.getMaxNumberOfGuests(), apartment.getTitle() ,apartmentComfortRepository.getAll(), apartment.getNumberOfRooms());

        model.addAttribute("changeApartmentInfoForm", changeApartmentInfoForm);
        model.addAttribute("selectedChosen", apartment.getApartmentComforts());

        if(apartment.getAvailableToGuest().getName().equals("Private room")) {
            model.addAttribute("guestsInRooms", apartment.getRooms());
            model.addAttribute("availableToGuests", "Private room");
        }

        return "/apartment/changeApartmentInfo";
    }

    @PostMapping("/change-apartment-info")
    public String changeApartmentInfoSave(@Valid ChangeApartmentInfoForm changeApartmentInfoForm, BindingResult bindingResult, Model model){
        Apartment apartment = apartmentRepository.findById(changeApartmentInfoForm.getId()).get();

        if(bindingResult.hasErrors()){
            changeApartmentInfoForm.setComforts(new ArrayList<>(apartment.getApartmentComforts()));
            model.addAttribute("changeApartmentInfoForm", changeApartmentInfoForm);
            model.addAttribute("selectedChosen", apartment.getApartmentComforts());

            if(apartment.getAvailableToGuest().getName().equals("Private room")) {
                model.addAttribute("guestsInRooms", apartment.getRooms());
                model.addAttribute("availableToGuests", "Private room");
            }

            return "/apartment/changeApartmentInfo";
        }

        if(apartment.getAvailableToGuest().getName().equals("Private room") && changeApartmentInfoForm.getMaxNumberOfGuests() != apartment.getMaxNumberOfGuests()){
            changedMaxNumberOfGuestsInEveryRoom(apartment.getId(), changeApartmentInfoForm.getGuestsInRoom());
        }

        apartment.setMaxNumberOfGuests(changeApartmentInfoForm.getMaxNumberOfGuests());
        apartment.setTitle(changeApartmentInfoForm.getTitle());
        apartment.setDescription(changeApartmentInfoForm.getDescription());
        apartment.setPrice(changeApartmentInfoForm.getPrice().floatValue());
        updateComfortsInApartment(apartment, changeApartmentInfoForm.getSelectedComforts());

        return "redirect:/my-advertisements";
    }

    @GetMapping("/delete-apartment/{apartment}")
    public String deleteApartment(Apartment apartment, Model model){
        DeleteApartmentForm deleteApartmentForm = new DeleteApartmentForm(apartment.getId());
        model.addAttribute("apartment", apartment);
        model.addAttribute("deleteApartmentForm", deleteApartmentForm);

        return "/apartment/deleteApartment";
    }

    @PostMapping("/delete-apartment")
    public String deleteApartmentConfirm(DeleteApartmentForm deleteApartmentForm){
        Apartment apartment = apartmentRepository.findById(deleteApartmentForm.getApartmentId()).get();

        if(apartment == null){
            return "redirect:/my-advertisements";
        }

        Set<ApartmentCalendar> canceled = apartment.getCalendars();
        setApartmentOrderToCancel(canceled);
        for(ApartmentCalendar apartmentCalendar : canceled){
            apartmentCalendar.setCanceled(true);
        }
        sendEmailsAboutRemoveApartment(canceled);
        apartment.setActive(false);

        return "redirect:/my-advertisements";
    }

    private boolean hasBooking(Apartment apartment){
        return !apartment.getCalendars().isEmpty();
    }

    private void sendEmailsAboutChangedLocation(Apartment apartment , String newLocation){
        List<ApartmentCalendar> booking = apartmentCalendarRepository.getFutureBooking(apartment.getId(), java.sql.Date.valueOf(LocalDate.now()));
        Set<String> uniqueEmails = getUniqueEmails(booking);

        for(String email : uniqueEmails){
            MailDto mail = new MailDto();
            mail.setFrom("best-rent.tk");
            mail.setSubject("BookingDto address changed");
            mail.setTo(email);
            Map<String, Object> model = new HashMap<>();
            model.put("from", apartment.getLocation());
            model.put("to", newLocation);
            model.put("user", apartment.getUser());
            model.put("signature", "https://best-rent.tk");
            mail.setModel(model);
            emailService.sendEmail(mail, "email/change-apartment-location");
        }

    }

    private void sendEmailsAboutRemoveApartment(Set<ApartmentCalendar> canceled){
        for (ApartmentCalendar apartmentCalendar : canceled) {
            MailDto mail = new MailDto();
            mail.setFrom("best-rent.tk");
            mail.setSubject("Your reservation has been canceled");
            mail.setTo(apartmentCalendar.getUser().getEmail());
            Map<String, Object> model = new HashMap<>();
            model.put("address", apartmentCalendar.getApartment().getLocation());
            model.put("user", apartmentCalendar.getUser());
            model.put("signature", "https://best-rent.tk");
            mail.setModel(model);
            emailService.sendEmail(mail, "email/cancel-reservation");
        }
    }

    private void setApartmentOrderToCancel(Set<ApartmentCalendar> canceled){
        for(ApartmentCalendar apartmentCalendar : canceled){
            apartmentCalendar.setCanceled(true);
        }
    }

    private Set<String> getUniqueEmails(List<ApartmentCalendar> booking) {
        Set<String> uniqueEmails = new HashSet<>();
        for(ApartmentCalendar apartmentCalendar : booking){
            uniqueEmails.add(apartmentCalendar.getUser().getEmail());
        }

        return uniqueEmails;
    }

    private void changedMaxNumberOfGuestsInEveryRoom(int apartmentId, List<Integer> numberOfGuestsInEveryRoom){
        List<Room> rooms = roomRepository.getAllRoomsByApartmentId(apartmentId);

        for(int i = 0; i < numberOfGuestsInEveryRoom.size(); i++){
            rooms.get(i).setMaxNumberOfGuests(numberOfGuestsInEveryRoom.get(i));
        }

        roomRepository.saveAll(rooms);
    }

    private void updateComfortsInApartment(Apartment apartment, List<Integer> selectedComforts){
        apartment.getApartmentComforts().clear();
        Set<ApartmentComfort> comforts = new HashSet<>();

        for(int comfort : selectedComforts){
            comforts.add(apartmentComfortRepository.getOne(comfort));
        }

        apartment.setApartmentComforts(comforts);
    }
}
