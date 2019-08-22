package rent.service.booking;

import org.springframework.beans.factory.annotation.Autowired;
import rent.dto.BookingInfoDto;
import rent.dto.BookingResultDto;
import rent.entities.ApartmentCalendar;
import rent.repository.ApartmentCalendarRepository;
import rent.repository.ApartmentRepository;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractBooking {
    @Autowired
    protected ApartmentRepository apartmentRepository;
    @Autowired
    protected ApartmentCalendarRepository apartmentCalendarRepository;

    public abstract BookingResultDto booking(BookingInfoDto bookingInfoDto);
    public abstract List<LocalDate> getBlockedDates(Set<ApartmentCalendar> calendars, BookingInfoDto bookingInfoDto);

    protected List<LocalDate> getDatesFromArriveToDeparture(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> returnsDate;
        long numOfDaysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        returnsDate = IntStream.iterate(0, i -> i + 1)
                .limit(numOfDaysBetween)
                .mapToObj(i -> startDate.plusDays(i))
                .collect(Collectors.toList());

        returnsDate.add(endDate);
        return returnsDate;
    }

    protected boolean isApartmentFirstDayFreeAndArriveEqualsBookingEndDate(ApartmentCalendar order, Date end){
        return order.isFirstDayFree() && order.getArrival().equals(end);
    }

    protected boolean isApartmentLastDayFreeAndDepartureEqualsBookingStartDate(ApartmentCalendar order, Date start){
        return order.isLastDayFree() && order.getDeparture().equals(start);
    }

    protected boolean isOneOrderFallInArriveDateAndAnotherFallInDepartureDate(ApartmentCalendar firstOrder, ApartmentCalendar secondOrder, BookingInfoDto bookingInfoDto){
        return isApartmentLastDayFreeAndDepartureEqualsBookingStartDate(firstOrder, bookingInfoDto.getStartDate()) && isApartmentFirstDayFreeAndArriveEqualsBookingEndDate(secondOrder, bookingInfoDto.getEndDate()) ||
                isApartmentLastDayFreeAndDepartureEqualsBookingStartDate(secondOrder, bookingInfoDto.getStartDate()) && isApartmentFirstDayFreeAndArriveEqualsBookingEndDate(firstOrder, bookingInfoDto.getEndDate());
    }
}
