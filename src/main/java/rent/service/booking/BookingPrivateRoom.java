package rent.service.booking;

import org.springframework.stereotype.Service;
import rent.dto.BookingInfoDto;
import rent.dto.BookingResultDto;
import rent.entities.Apartment;
import rent.entities.ApartmentCalendar;
import rent.entities.Room;

import java.time.LocalDate;
import java.util.*;

@Service
public class BookingPrivateRoom extends AbstractBooking {

    @Override
    public BookingResultDto booking(BookingInfoDto bookingInfoDto) {
        Apartment apartment = apartmentRepository.findById(bookingInfoDto.getApartmentId()).get();

        if(apartmentHaveRoomWhereMaxGuestsEqualsGuests(apartment.getRooms(), bookingInfoDto.getGuestsCount())){
            return findOneRoomByNumberOfGuests(apartment, bookingInfoDto);
        } else {
            return findMoreThanOneRoomForGuests(apartment, bookingInfoDto);
        }
    }

    private BookingResultDto findOneRoomByNumberOfGuests(Apartment apartment, BookingInfoDto bookingInfoDto){
        for(Room room : apartment.getRooms()){
            if(bookingInfoDto.getGuestsCount() <= room.getMaxNumberOfGuests()){
                List<ApartmentCalendar> betweenDates = apartmentCalendarRepository.checkBetweenDatesPrivateRoom(bookingInfoDto.getApartmentId(), bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), room.getId());

                if(betweenDates.isEmpty()){
                    apartmentCalendarRepository.save(new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), true, true,
                            bookingInfoDto.getGuestsCount(), apartment, bookingInfoDto.getUser(), room, false, bookingInfoDto.getPrice()));
                    return new BookingResultDto("Reservation is successful");
                }

                if(betweenDates.size() == 1){
                    final ApartmentCalendar order = betweenDates.get(0);
                    if(isApartmentFirstDayFreeAndArriveEqualsBookingEndDate(order, bookingInfoDto.getEndDate())){
                        order.setFirstDayFree(false);
                        apartmentCalendarRepository.save(order);
                        apartmentCalendarRepository.save(new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), true, false,
                                bookingInfoDto.getGuestsCount(), apartment, bookingInfoDto.getUser(), room, false, bookingInfoDto.getPrice()));
                        return new BookingResultDto("Reservation is successful");
                    } else if(isApartmentLastDayFreeAndDepartureEqualsBookingStartDate(order, bookingInfoDto.getStartDate())){
                        order.setLastDayFree(false);
                        apartmentCalendarRepository.save(order);
                        apartmentCalendarRepository.save(new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), false, true,
                                bookingInfoDto.getGuestsCount(), apartment, bookingInfoDto.getUser(), room, false, bookingInfoDto.getPrice()));
                        return new BookingResultDto( "Reservation is successful");
                    }
                }else if(isOneOrderFallInArriveDateAndAnotherFallInDepartureDate(betweenDates.get(0), betweenDates.get(1), bookingInfoDto)){
                    betweenDates.get(0).setLastDayFree(false);
                    apartmentCalendarRepository.save(betweenDates.get(0));
                    betweenDates.get(1).setFirstDayFree(false);
                    apartmentCalendarRepository.save(betweenDates.get(1));
                    apartmentCalendarRepository.save(new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), false, false,
                            bookingInfoDto.getGuestsCount(), apartment, bookingInfoDto.getUser(), room, false, bookingInfoDto.getPrice()));

                    return new BookingResultDto("Reservation is successful");
                }
            }
        }

        return new BookingResultDto("Sorry these dates reserved");
    }

    private BookingResultDto findMoreThanOneRoomForGuests(Apartment apartment, BookingInfoDto bookingInfoDto){
        List<ApartmentCalendar> newCalendars = new ArrayList<>();
        List<ApartmentCalendar> changedCalendars = new ArrayList<>();
        int guestsInReservedRoom = 0;
        int guests = bookingInfoDto.getGuestsCount();

        for(Room room : apartment.getRooms()){
            if(guestsInReservedRoom >= bookingInfoDto.getGuestsCount()){
                break;
            }

            guests -= room.getMaxNumberOfGuests();
            int guestSopped = guests >= 0 ? room.getMaxNumberOfGuests() : room.getMaxNumberOfGuests() - (guests * -1);

            List<ApartmentCalendar> betweenDates = apartmentCalendarRepository.checkBetweenDatesPrivateRoom(bookingInfoDto.getApartmentId(), bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), room.getId());

            if(betweenDates.isEmpty()){
                newCalendars.add(new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), true, true,
                        guestSopped, apartment, bookingInfoDto.getUser(), room, false, bookingInfoDto.getPrice()));
                guestsInReservedRoom += room.getMaxNumberOfGuests();
            } else if(betweenDates.size() == 1){
                final ApartmentCalendar order = betweenDates.get(0);
                if(isApartmentFirstDayFreeAndArriveEqualsBookingEndDate(order, bookingInfoDto.getEndDate())){
                    order.setFirstDayFree(false);
                    changedCalendars.add(order);
                    newCalendars.add(new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), true, false,
                            guestSopped, apartment, bookingInfoDto.getUser(), room, false, bookingInfoDto.getPrice()));
                    guestsInReservedRoom += room.getMaxNumberOfGuests();
                } else if(isApartmentLastDayFreeAndDepartureEqualsBookingStartDate(order, bookingInfoDto.getStartDate())){
                    order.setLastDayFree(false);
                    changedCalendars.add(order);
                    newCalendars.add(new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), false, true,
                            guestSopped, apartment, bookingInfoDto.getUser(), room, false, bookingInfoDto.getPrice()));
                    guestsInReservedRoom += room.getMaxNumberOfGuests();
                }
            }else if(isOneOrderFallInArriveDateAndAnotherFallInDepartureDate(betweenDates.get(0), betweenDates.get(1), bookingInfoDto)){
                betweenDates.get(0).setLastDayFree(false);
                changedCalendars.add(betweenDates.get(0));
                betweenDates.get(1).setFirstDayFree(false);
                changedCalendars.add(betweenDates.get(1));
                newCalendars.add(new ApartmentCalendar(bookingInfoDto.getStartDate(), bookingInfoDto.getEndDate(), false, false,
                        guestSopped, apartment, bookingInfoDto.getUser(), room, false, bookingInfoDto.getPrice()));
                guestsInReservedRoom += room.getMaxNumberOfGuests();
            }
        }

        if(guestsInReservedRoom < bookingInfoDto.getGuestsCount()){
            return new BookingResultDto("Sorry don't have rooms for your guests count");
        }

        if(bookingInfoDto.isApprove()){
            if(!changedCalendars.isEmpty()) {
                apartmentCalendarRepository.saveAll(changedCalendars);
            }
            apartmentCalendarRepository.saveAll(newCalendars);
            return new BookingResultDto("Reservation is successful");
        }

        return new BookingResultDto("For your number of guests you will need booking more than one room.If you agree click reservation.");
    }

    private boolean apartmentHaveRoomWhereMaxGuestsEqualsGuests(Set<Room> rooms, int guestsCount){
        for(Room room : rooms){
            if(room.getMaxNumberOfGuests() == guestsCount){
                return true;
            }
        }

        return false;
    }

    @Override
    public List<LocalDate> getBlockedDates(Set<ApartmentCalendar> calendars, BookingInfoDto bookingInfoDto) {
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
            if(checkDate.getValue().size() == bookingInfoDto.getNumberOfRooms()){
                dates.add(checkDate.getKey());
            }
        }

        return dates;
    }
}
