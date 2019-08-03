package rent.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rent.dto.BookingResultDto;
import rent.entities.Apartment;
import rent.entities.ApartmentCalendar;
import rent.entities.Room;
import rent.entities.User;
import rent.dto.SharedRoomBookingDayInfoDto;
import rent.repository.ApartmentCalendarRepository;
import rent.repository.ApartmentRepository;

import java.time.LocalDate;
import java.util.*;
import java.sql.Date;

@Service
public class BookingService {
    @Autowired
    private ApartmentCalendarRepository apartmentCalendarRepository;
    @Autowired
    private ApartmentRepository apartmentRepository;

    public BookingResultDto bookingEntireApartment(int apartmentId, Date startDate, Date endDate, int guestsCount, User user, float price)
    {
       return null;
    }

    public BookingResultDto bookingSharedRoom(int apartmentId, Date startDate, Date endDate, int guestsCount, User user, float price, int maxNumberOfGuests){
        return null;
    }

    public boolean apartmentHaveRoomWhereMaxGuestsEqualsGuests(Set<Room> rooms, int guestsCount){
        for(Room room : rooms){
            if(room.getMaxNumberOfGuests() == guestsCount){
                return true;
            }
        }

        return false;
    }

    public String bookingPrivateRoom(){
        return null;
    }

    public List<LocalDate> getBlockedDatesInSharedRoom(Set<ApartmentCalendar> calendars, int maxNumberOfGuest){
       return null;
    }

    public List<LocalDate> getBlockedDatesInPrivateRoom(Set<ApartmentCalendar> calendars, int numberOfRooms){
       return null;
    }

    public List<LocalDate> getDatesFromArriveToDeparture(LocalDate startDate, LocalDate endDate) {
        return null;
    }

    public List<LocalDate> getBlockedDatesInEntireApartment(Set<ApartmentCalendar> calendars){
        return null;
    }
}
