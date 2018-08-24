package rent.service;

import org.apache.tomcat.jni.Local;
import org.springframework.stereotype.Service;
import rent.entities.ApartmentCalendar;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class BookingService {
    private final int REMOVE_FIRST_DATE = 0;

    public String BookingEntireApartment(){
        return null;
    }

    public String BookingSharedRoom(){
        return null;
    }

    public String BookingPrivateRoom(){
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
