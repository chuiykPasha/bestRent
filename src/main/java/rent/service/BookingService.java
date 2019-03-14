package rent.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rent.dto.BookingDto;
import rent.entities.Apartment;
import rent.entities.ApartmentCalendar;
import rent.entities.Room;
import rent.entities.User;
import rent.dto.SharedRoomBookingDayInfoDto;
import rent.repository.ApartmentCalendarRepository;
import rent.repository.ApartmentRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.sql.Date;

@Service
public class BookingService {
    @Autowired
    private ApartmentCalendarRepository apartmentCalendarRepository;
    @Autowired
    private ApartmentRepository apartmentRepository;

    private final int REMOVE_FIRST_DATE = 0;

    public BookingDto bookingEntireApartment(int apartmentId, Date startDate, Date endDate, int guestsCount, User user, float price)
    {
        List<ApartmentCalendar> betweenDates = apartmentCalendarRepository.checkBetweenDates(apartmentId, startDate, endDate);

        if(betweenDates.isEmpty()){
            ApartmentCalendar apartmentCalendar = new ApartmentCalendar(startDate, endDate, new Apartment(apartmentId),
                    true, true, guestsCount, user, price);
            apartmentCalendarRepository.save(apartmentCalendar);
            return new BookingDto(getBlockedDatesInEntireApartment(apartmentRepository.getOne(apartmentId).getCalendars()), "Reservation is successful");
        }

        if(betweenDates.size() == 1){
            if(betweenDates.get(0).isFirstDayFree() && betweenDates.get(0).getArrival().equals(endDate)){
                betweenDates.get(0).setFirstDayFree(false);
                apartmentCalendarRepository.save(betweenDates.get(0));
                apartmentCalendarRepository.save(new ApartmentCalendar(startDate, endDate, new Apartment(apartmentId),
                        true, false, guestsCount, user, price));

                return new BookingDto(getBlockedDatesInEntireApartment(apartmentRepository.getOne(apartmentId).getCalendars()) ,"Reservation is successful");
            } else if(betweenDates.get(0).isLastDayFree() && betweenDates.get(0).getDeparture().equals(startDate)){
                betweenDates.get(0).setLastDayFree(false);
                apartmentCalendarRepository.save(betweenDates.get(0));
                apartmentCalendarRepository.save(new ApartmentCalendar(startDate, endDate, new Apartment(apartmentId),
                        false, true, guestsCount, user, price));

                return new BookingDto(getBlockedDatesInEntireApartment(apartmentRepository.getOne(apartmentId).getCalendars()) ,"Reservation is successful");
            } else {
                return new BookingDto("Sorry these dates reserved");
            }
        }else if(betweenDates.size() == 2 && (betweenDates.get(0).getDeparture().equals(startDate) && betweenDates.get(0).isLastDayFree()) &&
                (betweenDates.get(1).isFirstDayFree() && betweenDates.get(1).getArrival().equals(endDate)) ||
                betweenDates.size() == 2 && (betweenDates.get(1).getDeparture().equals(startDate) && betweenDates.get(1).isLastDayFree()) &&
                        (betweenDates.get(0).isFirstDayFree() && betweenDates.get(0).getArrival().equals(endDate))){
            betweenDates.get(0).setLastDayFree(false);
            apartmentCalendarRepository.save(betweenDates.get(0));
            betweenDates.get(1).setFirstDayFree(false);
            apartmentCalendarRepository.save(betweenDates.get(1));
            apartmentCalendarRepository.save(new ApartmentCalendar(startDate, endDate, new Apartment(apartmentId),
                    false, false, guestsCount, user, price));

            return new BookingDto(getBlockedDatesInEntireApartment(apartmentRepository.getOne(apartmentId).getCalendars()) ,"Reservation is successful");
        }

        return new BookingDto("Sorry these dates reserved");
    }

    public BookingDto bookingSharedRoom(int apartmentId, Date startDate, Date endDate, int guestsCount, User user, float price, int maxNumberOfGuests){
        List<ApartmentCalendar> betweenDates = apartmentCalendarRepository.checkDatesSharedRoom(apartmentId, startDate, endDate);

        if (betweenDates.isEmpty()) {
            ApartmentCalendar apartmentCalendar = new ApartmentCalendar(startDate, endDate, new Apartment(apartmentId),
                    true, true, guestsCount, user, price);
            apartmentCalendarRepository.save(apartmentCalendar);

            return new BookingDto(getBlockedDatesInSharedRoom(apartmentRepository.getOne(apartmentId).getCalendars(), maxNumberOfGuests), "Reservation is successful");
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

        List<LocalDate> checkSpaces = getDatesFromArriveToDeparture(startDate.toLocalDate(), endDate.toLocalDate());

        for (LocalDate date : checkSpaces) {
            if (daysInfo.containsKey(date)) {
                SharedRoomBookingDayInfoDto info = daysInfo.get(date);
                int freeSpaces = maxNumberOfGuests - info.getArriveGuests() - info.getCurrentGuests() + info.getDepartureGuests();

                if (freeSpaces < guestsCount) {
                    return new BookingDto("Sorry these dates reserved");
                }
            }
        }


        ApartmentCalendar apartmentCalendar = new ApartmentCalendar(startDate, endDate, new Apartment(apartmentId),
                true, true, guestsCount, user, price);
        apartmentCalendarRepository.save(apartmentCalendar);
        return new BookingDto(getBlockedDatesInSharedRoom(apartmentRepository.getOne(apartmentId).getCalendars(), maxNumberOfGuests), "Reservation is successful");
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

    public List<LocalDate> getBlockedDatesInEntireApartment(Set<ApartmentCalendar> calendars){
        List<LocalDate> dates = new ArrayList<>();

        for (ApartmentCalendar calendar : calendars) {
            List<LocalDate> datesBetween = getDatesFromArriveToDeparture(calendar.getArrival().toLocalDate(), calendar.getDeparture().toLocalDate());
            datesBetween.remove(REMOVE_FIRST_DATE);
            datesBetween.remove(datesBetween.size() - 1);
            dates.addAll(datesBetween);

            if (!calendar.isFirstDayFree()) {
                dates.add(calendar.getArrival().toLocalDate());
            }

            if (!calendar.isLastDayFree()) {
                dates.add(calendar.getDeparture().toLocalDate());
            }
        }

        return dates;
    }

    public List<LocalDate> getBlockedDatesInSharedRoom(Set<ApartmentCalendar> calendars, int maxNumberOfGuest){
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
            if(checkDate.getValue() == maxNumberOfGuest){
                dates.add(checkDate.getKey());
            }
        }

        return dates;
    }

    public List<LocalDate> getBlockedDatesInPrivateRoom(Set<ApartmentCalendar> calendars, int numberOfRooms){
        List<LocalDate> dates = new ArrayList<>();
        Map<LocalDate, List<Room>> checkDates = new HashMap<>();

        for(ApartmentCalendar calendar : calendars){
            List<LocalDate> datesBetween = getDatesFromArriveToDeparture(calendar.getArrival().toLocalDate(), calendar.getDeparture().toLocalDate());

            for(LocalDate date : datesBetween){
                if(checkDates.containsKey(date)){
                    checkDates.get(date).add(calendar.getRoom());
                    continue;
                }

                List<Room> rooms = new ArrayList<>();
                rooms.add(calendar.getRoom());
                checkDates.put(date, rooms);
            }
        }

        for(ApartmentCalendar lastDay : calendars){
            checkDates.get(lastDay.getDeparture().toLocalDate()).remove(lastDay.getRoom());
        }

        for(Map.Entry<LocalDate, List<Room>> checkDate : checkDates.entrySet()){
            if(checkDate.getValue().size() == numberOfRooms){
                dates.add(checkDate.getKey());
            }
        }

        return dates;
    }

    public List<LocalDate> getDatesFromArriveToDeparture(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> returnsDate;
        long numOfDaysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        returnsDate = IntStream.iterate(0, i -> i + 1)
                .limit(numOfDaysBetween)
                .mapToObj(i -> startDate.plusDays(i))
                .collect(Collectors.toList());

        returnsDate.add(endDate);
        return returnsDate;
    }
}
