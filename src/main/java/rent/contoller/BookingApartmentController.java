package rent.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import rent.dto.BookingInfoDto;
import rent.dto.BookingResultDto;
import rent.entities.Apartment;
import rent.entities.ApartmentCalendar;
import rent.entities.Room;
import rent.entities.User;
import rent.repository.ApartmentCalendarRepository;
import rent.service.BookingService;
import rent.service.booking.BookingEntireApartment;
import rent.service.booking.BookingPrivateRoom;
import rent.service.booking.BookingSharedRoom;
import rent.service.booking.BookingType;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class BookingApartmentController {
    @Autowired
    private ApartmentCalendarRepository apartmentCalendarRepository;
    private final int SIZE_HISTORY_IN_PAGE = 5;
    @Autowired
    private BookingService bookingService;

    @RequestMapping(value = "/apartment-booking", method = RequestMethod.POST)
    public @ResponseBody
    BookingResultDto apartmentBooking(@AuthenticationPrincipal User user, @RequestParam String bookingDates, @RequestParam int apartmentId,
                                      @RequestParam String availableToGuest, @RequestParam int guestsCount, @RequestParam int maxNumberOfGuests,
                                      @RequestParam float price, @RequestParam boolean approve) {
        final int START_DATE = 0;
        final int END_DATE = 1;

        if(bookingDates == null) {
            return new BookingResultDto("Wrong dates");
        }

        String [] dates = bookingDates.split(" - ");

        if(dates.length != 2) {
            return new BookingResultDto("Wrong dates");
        }

        final Date startDate = Date.valueOf(dates[START_DATE]);
        final Date endDate = Date.valueOf(dates[END_DATE]);

        if(availableToGuest.equals(BookingType.ENTIRE_APARTMENT.getType())) {
            BookingInfoDto bookingInfoDto = BookingInfoDto.builder()
                    .apartmentId(apartmentId).startDate(startDate).endDate(endDate)
                    .guestsCount(guestsCount).user(user).price(price).build();
           return bookingService.bookingEntireApartment(bookingInfoDto);
        } else if (availableToGuest.equals(BookingType.SHARED_ROOM.getType())) {
            BookingInfoDto bookingInfoDto = BookingInfoDto.builder()
                    .apartmentId(apartmentId).startDate(startDate).endDate(endDate)
                    .guestsCount(guestsCount).user(user).price(price).maxNumberOfGuests(maxNumberOfGuests).build();
            return bookingService.bookingSharedRoom(bookingInfoDto);
        } else if(availableToGuest.equals(BookingType.PRIVATE_ROOM.getType())) {
            BookingInfoDto bookingInfoDto = BookingInfoDto.builder()
                    .apartmentId(apartmentId).startDate(startDate).endDate(endDate)
                    .guestsCount(guestsCount).user(user).price(price).maxNumberOfGuests(maxNumberOfGuests).approve(approve).build();
            return bookingService.bookingPrivateRoom(bookingInfoDto);
        }

        return new BookingResultDto("ERROR");
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
