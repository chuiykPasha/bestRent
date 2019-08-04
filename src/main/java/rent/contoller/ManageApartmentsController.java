package rent.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

        Apartment changeApartment = apartmentRepository.getOne(changeLocationForm.getApartmentId());

        if(!changeApartment.getCalendars().isEmpty()){
            List<ApartmentCalendar> booking = apartmentCalendarRepository.getFutureBooking(changeLocationForm.getApartmentId(), java.sql.Date.valueOf(LocalDate.now()));
            Set<String> uniqueUsersEmail = new HashSet<>();
            for(ApartmentCalendar item : booking) {
                if(uniqueUsersEmail.contains(item.getUser().getEmail())){
                    continue;
                }

                MailDto mail = new MailDto();
                mail.setFrom("best-rent.tk");
                mail.setSubject("BookingDto address changed");
                mail.setTo(item.getUser().getEmail());
                Map<String, Object> model = new HashMap<>();
                model.put("from", changeApartment.getLocation());
                model.put("to", changeLocationForm.getLocation());
                model.put("user", changeApartment.getUser());
                model.put("signature", "https://best-rent.tk");
                mail.setModel(model);
                emailService.sendEmail(mail, "email/change-apartment-location");
                uniqueUsersEmail.add(item.getUser().getEmail());
            }
        }

        changeApartment.setLocation(changeLocationForm.getLocation());
        changeApartment.setLatitude(changeLocationForm.getLatitude());
        changeApartment.setLongitude(changeLocationForm.getLongitude());
        apartmentRepository.save(changeApartment);

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
    public String changeApartmentImagesSave(@Valid ChangeApartmentImagesForm changeApartmentImagesForm, BindingResult result, @AuthenticationPrincipal User user, Model model){
        if(changeApartmentImagesForm.getImages().isEmpty()){
            model.addAttribute("changeApartmentImagesForm", changeApartmentImagesForm);
            model.addAttribute("images", apartmentRepository.getOne(changeApartmentImagesForm.getApartmentId()));

            return "/apartment/changeApartmentImages";
        }

        uploadImageService.changeApartmentImages(changeApartmentImagesForm.getImages(), user.getEmail() ,changeApartmentImagesForm.getApartmentId(), changeApartmentImagesForm.getImagesSize());
        return "redirect:/my-advertisements";
    }

    @GetMapping("/change-apartment-info/{apartment}")
    public String changeApartmentInfo(Apartment apartment, Model model){
        ChangeApartmentInfoForm changeApartmentInfoForm = new ChangeApartmentInfoForm(apartment.getId(), apartment.getDescription(), BigDecimal.valueOf(apartment.getPrice()),
                apartment.getMaxNumberOfGuests(), apartment.getTitle() ,apartmentComfortRepository.findByIsActiveTrue(), apartment.getNumberOfRooms());

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
        if(bindingResult.hasErrors()){
            return "/apartment/changeApartmentInfo";
        }

        Apartment apartment = apartmentRepository.findById(changeApartmentInfoForm.getId()).get();

        if(apartment.getAvailableToGuest().getName().equals("Private room") && changeApartmentInfoForm.getMaxNumberOfGuests() != apartment.getMaxNumberOfGuests()){
            List<Room> rooms = roomRepository.findByApartmentId(changeApartmentInfoForm.getId());

            for(int i = 0; i < changeApartmentInfoForm.getGuestsInRoom().size(); i++){
                rooms.get(i).setMaxNumberOfGuests(changeApartmentInfoForm.getGuestsInRoom().get(i));
            }

            roomRepository.saveAll(rooms);
        }

        apartment.setMaxNumberOfGuests(changeApartmentInfoForm.getMaxNumberOfGuests());
        apartment.setTitle(changeApartmentInfoForm.getTitle());
        apartment.setDescription(changeApartmentInfoForm.getDescription());
        apartment.setPrice(changeApartmentInfoForm.getPrice().floatValue());

        apartment.getApartmentComforts().clear();
        Set<ApartmentComfort> comforts = new HashSet<>();

        for(int comfort : changeApartmentInfoForm.getSelectedComforts()){
            comforts.add(new ApartmentComfort(comfort));
        }

        apartment.setApartmentComforts(comforts);
        apartmentRepository.save(apartment);

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

        Set<String> emailsUsers = new HashSet<>();
        Set<ApartmentCalendar> canceled = new HashSet<>();

        for(ApartmentCalendar apartmentCalendar : apartment.getCalendars()){
            if(!emailsUsers.contains(apartmentCalendar.getUser().getEmail())){
                emailsUsers.add(apartmentCalendar.getUser().getEmail());
                canceled.add(apartmentCalendar);
            }

            apartmentCalendar.setCanceled(true);
        }

        Thread sendEmails = new Thread(){
            public void run(){
                for(ApartmentCalendar apartmentCalendar : canceled) {
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
        };

        sendEmails.start();

        apartment.setActive(false);
        apartmentRepository.save(apartment);

        return "redirect:/my-advertisements";
    }
}
