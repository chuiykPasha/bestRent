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
    private ApartmentComfortRepository apartmentComfortRepository;
    @Autowired
    private ApartmentRepository apartmentRepository;
    @Autowired
    private ApartmentCalendarRepository apartmentCalendarRepository;
    private final int sizeApartmentsInPage = 9;
    @Autowired
    private UploadImageService uploadImageService;
    private final String SHARED_ROOM = "Shared room";
    private final String ENTIRE_ROOM = "Entire apartment";
    private final String PRIVATE_ROOM = "Private room";
    private final int SIZE_HISTORY_IN_PAGE = 5;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingEntireApartment bookingEntireApartment;
    @Autowired
    private BookingSharedRoom bookingSharedRoom;

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

    @RequestMapping(value = "/apartment-booking", method = RequestMethod.POST)
    public @ResponseBody
    BookingResultDto apartmentBooking(@AuthenticationPrincipal User user, @RequestParam String bookingDates, @RequestParam int apartmentId,
                                      @RequestParam String availableToGuest, @RequestParam int guestsCount, @RequestParam int maxNumberOfGuests,
                                      @RequestParam float price, @RequestParam boolean approve) {
        final int START_DATE = 0;
        final int END_DATE = 1;
    /*
        if(bookingDates == null) {
            return new BookingResultDto("Wrong dates");
        }

        String [] dates = bookingDates.split(" - ");

        if(dates.length != 2) {
            return new BookingResultDto("Wrong dates");
        }

        final Date startDate = Date.valueOf(dates[START_DATE]);
        final Date endDate = Date.valueOf(dates[END_DATE]);

        if(availableToGuest.equals(ENTIRE_ROOM)) {
            BookingInfoDto bookingInfoDto = BookingInfoDto.builder()
                    .apartmentId(apartmentId).startDate(startDate).endDate(endDate)
                    .guestsCount(guestsCount).user(user).price(price).build();
           return bookingEntireApartment.booking(bookingInfoDto);
        } else if (availableToGuest.equals(SHARED_ROOM)) {
            BookingInfoDto bookingInfoDto = BookingInfoDto.builder()
                    .apartmentId(apartmentId).startDate(startDate).endDate(endDate)
                    .guestsCount(guestsCount).user(user).price(price).maxNumberOfGuests(maxNumberOfGuests).build();
            return bookingSharedRoom.booking(bookingInfoDto);
        } else if(availableToGuest.equals(PRIVATE_ROOM)){
            if(approve){
                if(!changedCalendars.isEmpty()) {
                    apartmentCalendarRepository.saveAll(changedCalendars);
                }
                apartmentCalendarRepository.saveAll(newCalendars);

                return new BookingResultDto(bookingService.getBlockedDatesInPrivateRoom(apartment.getCalendars(), apartment.getRooms().size()), "Reservation is successful");
            }
           return null;
        }
    */
        return new BookingResultDto("ERROR");
    }

    @GetMapping("my-advertisements")
    public String showMyAdvertisements(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("apartments", apartmentRepository.findByUserIdAndIsActiveTrueOrderByIdDesc(user.getId()));
        return "myAdvertisements";
    }

    @GetMapping("owner-rent-history")
    public String ownerRentHistory(@AuthenticationPrincipal User user, @RequestParam(name = "page", required = false) Integer page, Model model){
        int pageNumber = page != null ? page - 1 : 0;
        List<Integer> ownerApartmentsId = user.getApartments().stream().map((s) -> s.getId()).collect(Collectors.toList());
        List<ApartmentCalendar> rent = apartmentCalendarRepository.findByApartmentIdIn(ownerApartmentsId, PageRequest.of(pageNumber, SIZE_HISTORY_IN_PAGE, Sort.Direction.DESC, "id"));
        final int countPage = (int)Math.ceil(apartmentCalendarRepository.ownerRentHistoryCount(ownerApartmentsId) / (double)SIZE_HISTORY_IN_PAGE);
        model.addAttribute("rent", rent);
        model.addAttribute("defaultAvatar", User.DEFAULT_AVATAR);
        model.addAttribute("countPage", countPage);
        model.addAttribute("current", pageNumber);

        return "/apartment/ownerRentHistory";
    }


    @GetMapping("client-booking-history")
    public String clientBookingHistory(@AuthenticationPrincipal User user, @RequestParam(name = "page", required = false) Integer page, Model model){
        final int pageNumber = page != null ? page - 1 : 0;
        final int countPage = (int)Math.ceil(apartmentCalendarRepository.clientBookingHistoryCount(user.getId()) / (double)SIZE_HISTORY_IN_PAGE);
        List<ApartmentCalendar> booking = apartmentCalendarRepository.findByUserId(user.getId(), PageRequest.of(pageNumber, SIZE_HISTORY_IN_PAGE, Sort.Direction.DESC, "id"));
        model.addAttribute("booking", booking);
        model.addAttribute("defaultAvatar", User.DEFAULT_AVATAR);
        model.addAttribute("countPage", countPage);
        model.addAttribute("current", pageNumber);

        return "/apartment/clientBookingHistory";
    }
}

