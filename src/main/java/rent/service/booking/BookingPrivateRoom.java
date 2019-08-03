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
        /*
        Apartment apartment = apartmentRepository.getOne(bookingInfoDto.getApartmentId());

        //check if apartment have equals room
        if(bookingService.apartmentHaveRoomWhereMaxGuestsEqualsGuests(apartment.getRooms(), bookingInfoDto.getGuestsCount())){
            for(Room room : apartment.getRooms()){
                if(bookingInfoDto.getGuestsCount() <= room.getMaxNumberOfGuests()){
                    List<ApartmentCalendar> betweenDates = apartmentCalendarRepository.checkBetweenDatesPrivateRoom(bookingInfoDto.getApartmentId(), startDate, endDate, room.getId());

                    if(betweenDates.isEmpty()){
                        apartmentCalendarRepository.save(new ApartmentCalendar(startDate, endDate, true, true,
                                bookingInfoDto.getGuestsCount(), apartment, user, room, false, price));
                        return new BookingResultDto(bookingService.getBlockedDatesInPrivateRoom(apartment.getCalendars(), apartment.getRooms().size()), "Reservation is successful");
                    }

                    if(betweenDates.size() == 1){
                        if(betweenDates.get(0).isFirstDayFree() && betweenDates.get(0).getArrival().equals(endDate)){
                            betweenDates.get(0).setFirstDayFree(false);
                            apartmentCalendarRepository.save(betweenDates.get(0));
                            apartmentCalendarRepository.save(new ApartmentCalendar(startDate, endDate, true, false,
                                    bookingInfoDto.getGuestsCount(), apartment, user, room, false, price));
                            return new BookingResultDto(bookingService.getBlockedDatesInPrivateRoom(apartment.getCalendars(), apartment.getRooms().size()), "Reservation is successful");
                        } else if(betweenDates.get(0).isLastDayFree() && betweenDates.get(0).getDeparture().equals(startDate)){
                            betweenDates.get(0).setLastDayFree(false);
                            apartmentCalendarRepository.save(betweenDates.get(0));
                            apartmentCalendarRepository.save(new ApartmentCalendar(startDate, endDate, false, true,
                                    bookingInfoDto.getGuestsCount(), apartment, user, room, false, price));
                            return new BookingResultDto(bookingService.getBlockedDatesInPrivateRoom(apartment.getCalendars(), apartment.getRooms().size()), "Reservation is successful");
                        }
                    }else if(betweenDates.size() == 2 && (betweenDates.get(0).getDeparture().equals(startDate) && betweenDates.get(0).isLastDayFree()) &&
                            (betweenDates.get(1).isFirstDayFree() && betweenDates.get(1).getArrival().equals(endDate)) ||
                            betweenDates.size() == 2 && (betweenDates.get(1).getDeparture().equals(startDate) && betweenDates.get(1).isLastDayFree()) &&
                                    (betweenDates.get(0).isFirstDayFree() && betweenDates.get(0).getArrival().equals(endDate))){
                        betweenDates.get(0).setLastDayFree(false);
                        apartmentCalendarRepository.save(betweenDates.get(0));
                        betweenDates.get(1).setFirstDayFree(false);
                        apartmentCalendarRepository.save(betweenDates.get(1));
                        apartmentCalendarRepository.save(new ApartmentCalendar(startDate, endDate, false, false,
                                bookingInfoDto.getGuestsCount(), apartment, user, room, false, price));

                        return new BookingResultDto(bookingService.getBlockedDatesInPrivateRoom(apartment.getCalendars(), apartment.getRooms().size()), "Reservation is successful");
                    }
                }
            }
        } else {
            //for find more one room and check date
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

                List<ApartmentCalendar> betweenDates = apartmentCalendarRepository.checkBetweenDatesPrivateRoom(bookingInfoDto.getApartmentId(), startDate, endDate, room.getId());

                if(betweenDates.isEmpty()){
                    newCalendars.add(new ApartmentCalendar(startDate, endDate, true, true,
                            guestSopped, apartment, user, room, false, price));
                    guestsInReservedRoom += room.getMaxNumberOfGuests();
                } else if(betweenDates.size() == 1){
                    if(betweenDates.get(0).isFirstDayFree() && betweenDates.get(0).getArrival().equals(endDate)){
                        betweenDates.get(0).setFirstDayFree(false);
                        changedCalendars.add(betweenDates.get(0));
                        newCalendars.add(new ApartmentCalendar(startDate, endDate, true, false,
                                guestSopped, apartment, user, room, false, price));
                        guestsInReservedRoom += room.getMaxNumberOfGuests();
                    } else if(betweenDates.get(0).isLastDayFree() && betweenDates.get(0).getDeparture().equals(startDate)){
                        betweenDates.get(0).setLastDayFree(false);
                        changedCalendars.add(betweenDates.get(0));
                        newCalendars.add(new ApartmentCalendar(startDate, endDate, false, true,
                                guestSopped, apartment, user, room, false, price));
                        guestsInReservedRoom += room.getMaxNumberOfGuests();
                    }
                }else if(betweenDates.size() == 2 && (betweenDates.get(0).getDeparture().equals(startDate) && betweenDates.get(0).isLastDayFree()) &&
                        (betweenDates.get(1).isFirstDayFree() && betweenDates.get(1).getArrival().equals(endDate)) ||
                        betweenDates.size() == 2 && (betweenDates.get(1).getDeparture().equals(startDate) && betweenDates.get(1).isLastDayFree()) &&
                                (betweenDates.get(0).isFirstDayFree() && betweenDates.get(0).getArrival().equals(endDate))){
                    betweenDates.get(0).setLastDayFree(false);
                    changedCalendars.add(betweenDates.get(0));
                    betweenDates.get(1).setFirstDayFree(false);
                    changedCalendars.add(betweenDates.get(1));
                    newCalendars.add(new ApartmentCalendar(startDate, endDate, false, false,
                            guestSopped, apartment, user, room, false, bookingInfoDto.getPrice()));
                    guestsInReservedRoom += room.getMaxNumberOfGuests();
                }
            }

            if(guestsInReservedRoom < bookingInfoDto.getGuestsCount()){
                return new BookingResultDto("Sorry don't have rooms for your guests count");
            }

            return new BookingResultDto("For your number of guests you will need " + newCalendars.size() + " rooms and the price will be " + bookingInfoDto.getPrice() * newCalendars.size() + " . Will you booking?");
        }
            */
        return new BookingResultDto("Sorry these dates reserved");
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
