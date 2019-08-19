package rent.service.booking;

import org.springframework.stereotype.Service;
import rent.dto.BookingInfoDto;
import rent.dto.BookingResultDto;
import rent.entities.Apartment;
import rent.entities.ApartmentCalendar;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class BookingEntireApartment extends AbstractBooking {
    @Override
    public BookingResultDto booking(BookingInfoDto bookingInfoDto) {
        List<ApartmentCalendar> orders = apartmentCalendarRepository.getAllBookingThatFallBetweenArriveAndDeparture(bookingInfoDto.getApartmentId(), bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate());

        if(orders.isEmpty()){
            ApartmentCalendar apartmentCalendar = new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), new Apartment(bookingInfoDto.getApartmentId()),
                    true, true, bookingInfoDto.getGuestsCount(), bookingInfoDto.getUser(), bookingInfoDto.getPrice());
            apartmentCalendarRepository.save(apartmentCalendar);
            return new BookingResultDto(getBlockedDates(apartmentRepository.getOne(bookingInfoDto.getApartmentId()).getCalendars(), bookingInfoDto), "Reservation is successful");
        }

        if(isOnlyOneApartmentFallBetweenArriveAndDeparture(orders.size())){
            return tryBookingWhenOneApartmentFallBetweenArriveAndDeparture(orders.get(0), bookingInfoDto);
        } else if (orders.size() == 2 && isOneOrderFallInArriveDateAndAnotherFallInDepartureDate(orders.get(0), orders.get(1), bookingInfoDto)) {
            return bookingWhenOrderFallBetweenTwoApartments(orders.get(0), orders.get(1), bookingInfoDto);
        }

        return new BookingResultDto("Sorry these dates reserved");
    }

    public List<LocalDate> getBlockedDates(Set<ApartmentCalendar> calendars, BookingInfoDto bookingInfoDto){
        List<LocalDate> dates = new ArrayList<>();

        for (ApartmentCalendar calendar : calendars) {
            List<LocalDate> datesBetween = getDatesBetweenArriveAndDeparture(calendar.getArrival().toLocalDate(), calendar.getDeparture().toLocalDate());
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

    private List<LocalDate> getDatesBetweenArriveAndDeparture(LocalDate arrive, LocalDate departure){
        final int FIRST_DATE = 0;
        List<LocalDate> result = getDatesFromArriveToDeparture(arrive, departure);
        result.remove(FIRST_DATE);
        final int lastDate = result.size() - 1;
        result.remove(lastDate);
        return result;
    }

    private boolean isOnlyOneApartmentFallBetweenArriveAndDeparture(int size){
        return size == 1;
    }

    private BookingResultDto tryBookingWhenOneApartmentFallBetweenArriveAndDeparture(ApartmentCalendar order, BookingInfoDto bookingInfoDto){
        if( isApartmentFirstDayFreeAndArriveEqualsBookingEndDate(order, bookingInfoDto.getEndDate())){
            order.setFirstDayFree(false);
            apartmentCalendarRepository.save(order);
            apartmentCalendarRepository.save(new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), new Apartment(bookingInfoDto.getApartmentId()),
                    true, false, bookingInfoDto.getGuestsCount(), bookingInfoDto.getUser(), bookingInfoDto.getPrice()));
            return new BookingResultDto(getBlockedDates(apartmentRepository.getOne(bookingInfoDto.getApartmentId()).getCalendars(), bookingInfoDto) ,"Reservation is successful");
        } else if(isApartmentLastDayFreeAndDepartureEqualsBookingStartDate(order, bookingInfoDto.getStartDate())){
            order.setLastDayFree(false);
            apartmentCalendarRepository.save(order);
            apartmentCalendarRepository.save(new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), new Apartment(bookingInfoDto.getApartmentId()),
                    false, true, bookingInfoDto.getGuestsCount(), bookingInfoDto.getUser(), bookingInfoDto.getPrice()));
            return new BookingResultDto(getBlockedDates(apartmentRepository.getOne(bookingInfoDto.getApartmentId()).getCalendars(), bookingInfoDto) ,"Reservation is successful");
        } else {
            return new BookingResultDto("Sorry these dates reserved");
        }
    }

    private boolean isApartmentFirstDayFreeAndArriveEqualsBookingEndDate(ApartmentCalendar order, Date end){
        return order.isFirstDayFree() && order.getArrival().equals(end);
    }

    private boolean isApartmentLastDayFreeAndDepartureEqualsBookingStartDate(ApartmentCalendar order, Date start){
        return order.isLastDayFree() && order.getDeparture().equals(start);
    }

    private BookingResultDto bookingWhenOrderFallBetweenTwoApartments(ApartmentCalendar firstOrder, ApartmentCalendar secondOrder, BookingInfoDto bookingInfoDto){
        firstOrder.setLastDayFree(false);
        apartmentCalendarRepository.save(firstOrder);
        secondOrder.setFirstDayFree(false);
        apartmentCalendarRepository.save(secondOrder);
        apartmentCalendarRepository.save(new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), new Apartment(bookingInfoDto.getApartmentId()),
                false, false, bookingInfoDto.getGuestsCount(), bookingInfoDto.getUser(), bookingInfoDto.getPrice()));
        return new BookingResultDto(getBlockedDates(apartmentRepository.getOne(bookingInfoDto.getApartmentId()).getCalendars(), bookingInfoDto), "Reservation is successful");
    }

    private boolean isOneOrderFallInArriveDateAndAnotherFallInDepartureDate(ApartmentCalendar firstOrder, ApartmentCalendar secondOrder, BookingInfoDto bookingInfoDto){
        return isApartmentLastDayFreeAndDepartureEqualsBookingStartDate(firstOrder, bookingInfoDto.getStartDate()) && isApartmentFirstDayFreeAndArriveEqualsBookingEndDate(secondOrder, bookingInfoDto.getEndDate()) ||
                isApartmentLastDayFreeAndDepartureEqualsBookingStartDate(secondOrder, bookingInfoDto.getStartDate()) && isApartmentFirstDayFreeAndArriveEqualsBookingEndDate(firstOrder, bookingInfoDto.getEndDate());
    }
}
