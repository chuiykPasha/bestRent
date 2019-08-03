package rent.service.booking;

import org.springframework.stereotype.Service;
import rent.dto.BookingInfoDto;
import rent.dto.BookingResultDto;
import rent.dto.SharedRoomBookingDayInfoDto;
import rent.entities.Apartment;
import rent.entities.ApartmentCalendar;

import java.time.LocalDate;
import java.util.*;

@Service
public class BookingSharedRoom extends AbstractBooking {
    @Override
    public BookingResultDto booking(BookingInfoDto bookingInfoDto) {
        List<ApartmentCalendar> betweenDates = apartmentCalendarRepository.checkDatesSharedRoom(bookingInfoDto.getApartmentId(), bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate());

        if (betweenDates.isEmpty()) {
            ApartmentCalendar apartmentCalendar = new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), new Apartment(bookingInfoDto.getApartmentId()),
                    true, true, bookingInfoDto.getGuestsCount(), bookingInfoDto.getUser(), bookingInfoDto.getPrice());
            apartmentCalendarRepository.save(apartmentCalendar);

            return new BookingResultDto(getBlockedDates(apartmentRepository.getOne(bookingInfoDto.getApartmentId()).getCalendars(), bookingInfoDto), "Reservation is successful");
        }

        Map<LocalDate, SharedRoomBookingDayInfoDto> daysInfo = new HashMap<>();

        for (ApartmentCalendar day : betweenDates) {
            List<LocalDate> fromArriveToDepartureDays = getDatesFromArriveToDeparture(day.getArrival().toLocalDate()
                    , day.getDeparture().toLocalDate());

            for (LocalDate checkDate : fromArriveToDepartureDays) {
                if (daysInfo.containsKey(checkDate)) {
                    if (checkDate.equals(day.getArrival().toLocalDate())) {
                        daysInfo.get(checkDate).plusArriveGuests(day.getCurrentCountGuest());
                    } else if (checkDate.equals(day.getDeparture().toLocalDate())) {
                        daysInfo.get(checkDate).plusDepartureGuests(day.getCurrentCountGuest());
                    } else {
                        daysInfo.get(checkDate).plusCurrentGuests(day.getCurrentCountGuest());
                    }
                } else {
                    if (checkDate.equals(day.getArrival().toLocalDate())) {
                        daysInfo.put(checkDate, new SharedRoomBookingDayInfoDto(0, day.getCurrentCountGuest(), 0));
                    } else if (checkDate.equals(day.getDeparture().toLocalDate())) {
                        daysInfo.put(checkDate, new SharedRoomBookingDayInfoDto(0, 0, day.getCurrentCountGuest()));
                    } else {
                        daysInfo.put(checkDate, new SharedRoomBookingDayInfoDto(day.getCurrentCountGuest(), 0, 0));
                    }
                }
            }
        }

        List<LocalDate> checkSpaces = getDatesFromArriveToDeparture(bookingInfoDto.getStartDate().toLocalDate(), bookingInfoDto.getEndDate().toLocalDate());

        for (LocalDate date : checkSpaces) {
            if (daysInfo.containsKey(date)) {
                SharedRoomBookingDayInfoDto info = daysInfo.get(date);
                int freeSpaces = bookingInfoDto.getMaxNumberOfGuests() - info.getArriveGuests() - info.getCurrentGuests() + info.getDepartureGuests();

                if (freeSpaces < bookingInfoDto.getGuestsCount()) {
                    return new BookingResultDto("Sorry these dates reserved");
                }
            }
        }


        ApartmentCalendar apartmentCalendar = new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), new Apartment(bookingInfoDto.getApartmentId()),
                true, true, bookingInfoDto.getGuestsCount(), bookingInfoDto.getUser(), bookingInfoDto.getPrice());
        apartmentCalendarRepository.save(apartmentCalendar);
        return new BookingResultDto(getBlockedDates(apartmentRepository.getOne(bookingInfoDto.getApartmentId()).getCalendars(), bookingInfoDto), "Reservation is successful");
    }

    @Override
    public List<LocalDate> getBlockedDates(Set<ApartmentCalendar> calendars, BookingInfoDto bookingInfoDto) {
        Map<LocalDate, Integer> checkDates = new HashMap<>();
        List<LocalDate> dates = new ArrayList<>();

        for(ApartmentCalendar calendar : calendars){
            List<LocalDate> datesBetween = getDatesFromArriveToDeparture(calendar.getArrival().toLocalDate(), calendar.getDeparture().toLocalDate());

            for(LocalDate date : datesBetween) {
                if(checkDates.containsKey(date)){
                    int countDate = checkDates.get(date).intValue();
                    checkDates.put(date, countDate + calendar.getCurrentCountGuest());
                    continue;
                }

                checkDates.put(date, calendar.getCurrentCountGuest());
            }
        }

        //checkLastDates
        for(ApartmentCalendar  lastDay : calendars){
            int countDate = checkDates.get(lastDay.getDeparture().toLocalDate()).intValue();
            countDate -= lastDay.getCurrentCountGuest();
            checkDates.put(lastDay.getDeparture().toLocalDate(), countDate);
        }


        //fill blocket dates
        for(Map.Entry<LocalDate, Integer> checkDate : checkDates.entrySet()){
            if(checkDate.getValue() == bookingInfoDto.getMaxNumberOfGuests()){
                dates.add(checkDate.getKey());
            }
        }

        return dates;
    }
}
