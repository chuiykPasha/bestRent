package rent.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import rent.service.booking.BookingEntireApartment;
import rent.dto.BookingInfoDto;
import rent.dto.BookingResultDto;
import rent.dto.MailDto;
import rent.entities.*;
import rent.form.*;
import rent.repository.*;
import rent.service.BookingService;
import rent.service.EmailService;
import rent.service.UploadImageService;
import rent.service.booking.BookingSharedRoom;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ApartmentController {
    @Autowired
    private ApartmentRepository apartmentRepository;
    @Autowired
    private ApartmentCalendarRepository apartmentCalendarRepository;
    private final int sizeApartmentsInPage = 9;
    private final String SHARED_ROOM = "Shared room";
    private final String ENTIRE_ROOM = "Entire apartment";
    private final String PRIVATE_ROOM = "Private room";
    @Autowired
    private BookingService bookingService;

    @RequestMapping(value = {"/"}, method = RequestMethod.GET)
    public String main(@RequestParam(name = "location", required = false) String location, @RequestParam(name = "page", required = false) Integer page, Model model) {
        int pageNumber = page != null ? page - 1 : 0;

        if(location != null && location != "") {
            int countPage = (int)Math.ceil(apartmentRepository.countPageByLocation(location) / (double)sizeApartmentsInPage);
            model.addAttribute("location", location);
            model.addAttribute("apartments", apartmentRepository.getApartmentsByLocation(location, pageNumber * sizeApartmentsInPage, sizeApartmentsInPage));
            model.addAttribute("countPage", countPage);
            model.addAttribute("current", pageNumber);
        } else {
            int countPage = (int)Math.ceil(apartmentRepository.countActiveApartments() / (double)sizeApartmentsInPage);
            List<Apartment> apartments = apartmentRepository.findByIsActiveTrue(PageRequest.of(pageNumber, sizeApartmentsInPage, Sort.Direction.DESC, "id"));
            model.addAttribute("apartments", apartments);
            model.addAttribute("countPage", countPage);
            model.addAttribute("current", pageNumber);
            model.addAttribute("location", null);
        }

        return "index";
    }

    @GetMapping("/apartment/{apartment}")
    public String showApartmentById(Apartment apartment, Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("apartment", apartment);
        model.addAttribute("apartmentId", apartment.getId());
        model.addAttribute("defaultAvatar", User.DEFAULT_AVATAR);
        model.addAttribute("availableToGuest", apartment.getAvailableToGuest().getName());
        model.addAttribute("userOnPage", user != null? "userLogin" : "guest");
        model.addAttribute("userId", apartment.getUser().getId());
        model.addAttribute("price", apartment.getPrice());

        List<LocalDate> dates = new ArrayList<>();

        if (apartment.getAvailableToGuest().getName().equals(ENTIRE_ROOM)) {
            dates = bookingService.getBlockedDatesInEntireApartment(apartment.getCalendars());
        } else if(apartment.getAvailableToGuest().getName().equals(SHARED_ROOM)){
            dates = bookingService.getBlockedDatesInSharedRoom(apartment.getCalendars(), apartment.getMaxNumberOfGuests());
        } else if(apartment.getAvailableToGuest().getName().equals(PRIVATE_ROOM)){
            dates = bookingService.getBlockedDatesInPrivateRoom(apartment.getCalendars(), apartment.getRooms().size());
        }

        model.addAttribute("disabledDates", dates);
        return "/apartment/showApartment";
    }

    @GetMapping("my-advertisements")
    public String showMyAdvertisements(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("apartments", apartmentRepository.findByUserIdAndIsActiveTrueOrderByIdDesc(user.getId()));
        return "myAdvertisements";
    }
}

