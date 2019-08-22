package rent.service;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rent.dto.BookingInfoDto;
import rent.dto.BookingResultDto;
import rent.entities.Apartment;
import rent.entities.ApartmentCalendar;
import rent.entities.Room;
import rent.entities.User;
import rent.dto.SharedRoomBookingDayInfoDto;
import rent.repository.ApartmentCalendarRepository;
import rent.repository.ApartmentRepository;
import rent.service.booking.BookingEntireApartment;
import rent.service.booking.BookingPrivateRoom;
import rent.service.booking.BookingSharedRoom;

import java.time.LocalDate;
import java.util.*;
import java.sql.Date;

@Service
public class BookingService {
    @Autowired
    private BookingEntireApartment bookingEntireApartment;
    @Autowired
    private BookingSharedRoom bookingSharedRoom;
    @Autowired
    private BookingPrivateRoom bookingPrivateRoom;

    public BookingResultDto bookingEntireApartment(BookingInfoDto bookingInfoDto){
        return bookingEntireApartment.booking(bookingInfoDto);
    }

    public BookingResultDto bookingSharedRoom(BookingInfoDto bookingInfoDto){
        return bookingSharedRoom.booking(bookingInfoDto);
    }

    public BookingResultDto bookingPrivateRoom(BookingInfoDto bookingInfoDto){
        return bookingPrivateRoom.booking(bookingInfoDto);
    }

    public List<LocalDate> getBlockedDatesInSharedRoom(Set<ApartmentCalendar> calendars, int maxNumberOfGuest){
        BookingInfoDto bookingInfoDto = BookingInfoDto.builder().maxNumberOfGuests(maxNumberOfGuest).build();
        return bookingSharedRoom.getBlockedDates(calendars, bookingInfoDto);
    }

    public List<LocalDate> getBlockedDatesInPrivateRoom(Set<ApartmentCalendar> calendars, int numberOfRooms){
        BookingInfoDto bookingInfoDto = BookingInfoDto.builder().numberOfRooms(numberOfRooms).build();
        return bookingPrivateRoom.getBlockedDates(calendars, bookingInfoDto);
    }

    public List<LocalDate> getBlockedDatesInEntireApartment(Set<ApartmentCalendar> calendars){
        return bookingEntireApartment.getBlockedDates(calendars, null);
    }
}
