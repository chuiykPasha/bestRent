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
        List<ApartmentCalendar> betweenDates = apartmentCalendarRepository.getAllBookingThatFallOnDatesForSharedRoom(bookingInfoDto.getApartmentId(), bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate());

        if (betweenDates.isEmpty()) {
            ApartmentCalendar apartmentCalendar = new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), new Apartment(bookingInfoDto.getApartmentId()),
                    true, true, bookingInfoDto.getGuestsCount(), bookingInfoDto.getUser(), bookingInfoDto.getPrice());
            apartmentCalendarRepository.save(apartmentCalendar);
            return new BookingResultDto(getBlockedDates(apartmentRepository.getOne(bookingInfoDto.getApartmentId()).getCalendars(), bookingInfoDto), "Reservation is successful");
        }

        Map<LocalDate, SharedRoomBookingDayInfoDto> daysInfo  = getInfoAboutEveryDay(betweenDates);
        boolean isPlaceForGuests = isEmptyPlaceForGuests(daysInfo, bookingInfoDto);

        if(!isPlaceForGuests){
            return new BookingResultDto("Sorry these dates reserved");
        }

        ApartmentCalendar apartmentCalendar = new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), new Apartment(bookingInfoDto.getApartmentId()),
                true, true, bookingInfoDto.getGuestsCount(), bookingInfoDto.getUser(), bookingInfoDto.getPrice());
        apartmentCalendarRepository.save(apartmentCalendar);
        return new BookingResultDto(getBlockedDates(apartmentRepository.getOne(bookingInfoDto.getApartmentId()).getCalendars(), bookingInfoDto), "Reservation is successful");
    }

    private Map<LocalDate, SharedRoomBookingDayInfoDto> getInfoAboutEveryDay(List<ApartmentCalendar> betweenDates){
        Map<LocalDate, SharedRoomBookingDayInfoDto> daysInfo = new HashMap<>();

        for (ApartmentCalendar booking : betweenDates) {
            List<LocalDate> fromArriveToDepartureDays = getDatesFromArriveToDeparture(booking.getArrival().toLocalDate(), booking.getDeparture().toLocalDate());

            for (LocalDate checkDate : fromArriveToDepartureDays) {
                if (daysInfo.containsKey(checkDate)) {
                    updateDaysInfo(daysInfo, checkDate, booking);
                } else {
                    initNewDayInfo(daysInfo, checkDate, booking);
                }
            }
        }

        return daysInfo;
    }

    private void updateDaysInfo(Map<LocalDate, SharedRoomBookingDayInfoDto> daysInfo, LocalDate checkDate, ApartmentCalendar booking){
        if (checkDate.equals(booking.getArrival().toLocalDate())) {
            daysInfo.get(checkDate).plusArriveGuests(booking.getCurrentCountGuest());
        } else if (checkDate.equals(booking.getDeparture().toLocalDate())) {
            daysInfo.get(checkDate).plusDepartureGuests(booking.getCurrentCountGuest());
        } else {
            daysInfo.get(checkDate).plusCurrentGuests(booking.getCurrentCountGuest());
        }
    }

    private void initNewDayInfo(Map<LocalDate, SharedRoomBookingDayInfoDto> daysInfo, LocalDate checkDate, ApartmentCalendar booking){
        if (checkDate.equals(booking.getArrival().toLocalDate())) {
            daysInfo.put(checkDate, new SharedRoomBookingDayInfoDto(0, booking.getCurrentCountGuest(), 0));
        } else if (checkDate.equals(booking.getDeparture().toLocalDate())) {
            daysInfo.put(checkDate, new SharedRoomBookingDayInfoDto(0, 0, booking.getCurrentCountGuest()));
        } else {
            daysInfo.put(checkDate, new SharedRoomBookingDayInfoDto(booking.getCurrentCountGuest(), 0, 0));
        }
    }

    private boolean isEmptyPlaceForGuests(Map<LocalDate, SharedRoomBookingDayInfoDto> daysInfo, BookingInfoDto bookingInfoDto){
        List<LocalDate> checkSpaces = getDatesFromArriveToDeparture(bookingInfoDto.getStartDate().toLocalDate(), bookingInfoDto.getEndDate().toLocalDate());

        for (LocalDate date : checkSpaces) {
            if (daysInfo.containsKey(date)) {
                SharedRoomBookingDayInfoDto info = daysInfo.get(date);
                int freeSpaces = bookingInfoDto.getMaxNumberOfGuests() - info.getArriveGuests() - info.getCurrentGuests() + info.getDepartureGuests();

                if (freeSpaces < bookingInfoDto.getGuestsCount()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public List<LocalDate> getBlockedDates(Set<ApartmentCalendar> calendars, BookingInfoDto bookingInfoDto) {
        Map<LocalDate, Integer> daysInfo = getInfoAboutEveryDayForShowBlockedDates(calendars);
        updateEveryDepartureDay(daysInfo, calendars);
        return getBlockedDates(daysInfo, bookingInfoDto.getMaxNumberOfGuests());
    }

    private Map<LocalDate, Integer> getInfoAboutEveryDayForShowBlockedDates(Set<ApartmentCalendar> calendars){
        Map<LocalDate, Integer> everyDayInfo = new HashMap<>();

        for(ApartmentCalendar booking : calendars){
            List<LocalDate> datesBetween = getDatesFromArriveToDeparture(booking.getArrival().toLocalDate(), booking.getDeparture().toLocalDate());

            for(LocalDate date : datesBetween) {
                if(everyDayInfo.containsKey(date)){
                    int countDate = everyDayInfo.get(date);
                    everyDayInfo.put(date, countDate + booking.getCurrentCountGuest());
                    continue;
                }

                everyDayInfo.put(date, booking.getCurrentCountGuest());
            }
        }

        return everyDayInfo;
    }

    private void updateEveryDepartureDay(Map<LocalDate, Integer> daysInfo, Set<ApartmentCalendar> calendars){
        for(ApartmentCalendar  lastDay : calendars){
            int countDate = daysInfo.get(lastDay.getDeparture().toLocalDate());
            countDate -= lastDay.getCurrentCountGuest();
            daysInfo.put(lastDay.getDeparture().toLocalDate(), countDate);
        }
    }

    private List<LocalDate> getBlockedDates(Map<LocalDate, Integer> daysInfo, int maxNumberOfGuests){
        List<LocalDate> blockedDates = new ArrayList<>();

        for(Map.Entry<LocalDate, Integer> checkDate : daysInfo.entrySet()){
            if(checkDate.getValue() == maxNumberOfGuests){
                blockedDates.add(checkDate.getKey());
            }
        }

        return blockedDates;
    }
}

