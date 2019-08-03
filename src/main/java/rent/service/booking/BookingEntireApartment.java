package rent.service.booking;

import org.springframework.stereotype.Service;
import rent.dto.BookingInfoDto;
import rent.dto.BookingResultDto;
import rent.entities.Apartment;
import rent.entities.ApartmentCalendar;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class BookingEntireApartment extends AbstractBooking {
    @Override
    public BookingResultDto booking(BookingInfoDto bookingInfoDto) {
        List<ApartmentCalendar> betweenDates = apartmentCalendarRepository.checkBetweenDates(bookingInfoDto.getApartmentId(), bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate());

        if(betweenDates.isEmpty()){
            ApartmentCalendar apartmentCalendar = new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), new Apartment(bookingInfoDto.getApartmentId()),
                    true, true, bookingInfoDto.getGuestsCount(), bookingInfoDto.getUser(), bookingInfoDto.getPrice());
            apartmentCalendarRepository.save(apartmentCalendar);
            return new BookingResultDto(getBlockedDates(apartmentRepository.getOne(bookingInfoDto.getApartmentId()).getCalendars(), bookingInfoDto), "Reservation is successful");
        }

        if(betweenDates.size() == 1){
            if(betweenDates.get(0).isFirstDayFree() && betweenDates.get(0).getArrival().equals(bookingInfoDto.getEndDate())){
                betweenDates.get(0).setFirstDayFree(false);
                apartmentCalendarRepository.save(betweenDates.get(0));
                apartmentCalendarRepository.save(new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), new Apartment(bookingInfoDto.getApartmentId()),
                        true, false, bookingInfoDto.getGuestsCount(), bookingInfoDto.getUser(), bookingInfoDto.getPrice()));

                return new BookingResultDto(getBlockedDates(apartmentRepository.getOne(bookingInfoDto.getApartmentId()).getCalendars(), bookingInfoDto) ,"Reservation is successful");
            } else if(betweenDates.get(0).isLastDayFree() && betweenDates.get(0).getDeparture().equals(bookingInfoDto.getStartDate())){
                betweenDates.get(0).setLastDayFree(false);
                apartmentCalendarRepository.save(betweenDates.get(0));
                apartmentCalendarRepository.save(new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), new Apartment(bookingInfoDto.getApartmentId()),
                        false, true, bookingInfoDto.getGuestsCount(), bookingInfoDto.getUser(), bookingInfoDto.getPrice()));

                return new BookingResultDto(getBlockedDates(apartmentRepository.getOne(bookingInfoDto.getApartmentId()).getCalendars(), bookingInfoDto) ,"Reservation is successful");
            } else {
                return new BookingResultDto("Sorry these dates reserved");
            }
        }else if(betweenDates.size() == 2 && (betweenDates.get(0).getDeparture().equals(bookingInfoDto.getStartDate()) && betweenDates.get(0).isLastDayFree()) &&
                (betweenDates.get(1).isFirstDayFree() && betweenDates.get(1).getArrival().equals(bookingInfoDto.getEndDate())) ||
                betweenDates.size() == 2 && (betweenDates.get(1).getDeparture().equals(bookingInfoDto.getStartDate()) && betweenDates.get(1).isLastDayFree()) &&
                        (betweenDates.get(0).isFirstDayFree() && betweenDates.get(0).getArrival().equals(bookingInfoDto.getEndDate()))){
            betweenDates.get(0).setLastDayFree(false);
            apartmentCalendarRepository.save(betweenDates.get(0));
            betweenDates.get(1).setFirstDayFree(false);
            apartmentCalendarRepository.save(betweenDates.get(1));
            apartmentCalendarRepository.save(new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), new Apartment(bookingInfoDto.getApartmentId()),
                    false, false, bookingInfoDto.getGuestsCount(), bookingInfoDto.getUser(), bookingInfoDto.getPrice()));

            return new BookingResultDto(getBlockedDates(apartmentRepository.getOne(bookingInfoDto.getApartmentId()).getCalendars(), bookingInfoDto) ,"Reservation is successful");
        }

        return new BookingResultDto("Sorry these dates reserved");
    }

    public List<LocalDate> getBlockedDates(Set<ApartmentCalendar> calendars, BookingInfoDto bookingInfoDto){
        List<LocalDate> dates = new ArrayList<>();
        final int REMOVE_FIRST_DATE = 0;

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
}
