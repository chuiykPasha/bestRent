package rent.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rent.entities.Apartment;
import rent.entities.ApartmentCalendar;

import java.sql.Date;
import java.util.List;

public interface ApartmentCalendarRepository extends JpaRepository<ApartmentCalendar, Integer> {

    @Query("SELECT a FROM ApartmentCalendar a WHERE a.apartment.id = :apartmentId AND a.arrival BETWEEN :arrive AND :departure OR a.departure BETWEEN :arrive AND :departure AND a.apartment.id = :apartmentId")
    List<ApartmentCalendar> getAllBookingThatFallBetweenArriveAndDeparture(@Param("apartmentId") int apartmentId, @Param("arrive") Date arrive, @Param("departure") Date departure);

    @Query("SELECT a FROM ApartmentCalendar a WHERE a.apartment.id = :apartmentId AND a.arrival BETWEEN :arrive AND :departure AND a.departure BETWEEN :arrive AND :departure OR a.departure > :arrive AND a.apartment.id = :apartmentId")
    List<ApartmentCalendar> checkDatesSharedRoom(@Param("apartmentId") int apartmentId, @Param("arrive") Date arrive, @Param("departure") Date departure);

    @Query("SELECT a FROM ApartmentCalendar a WHERE a.apartment.id = :apartmentId AND a.room.id = :roomId AND a.arrival BETWEEN :arrive AND :departure OR a.departure BETWEEN :arrive AND :departure AND a.room.id = :roomId")
    List<ApartmentCalendar> checkBetweenDatesPrivateRoom(@Param("apartmentId") int apartmentId, @Param("arrive") Date arrive, @Param("departure") Date departure, @Param("roomId") int roomId);

    List<ApartmentCalendar> findByUserId(int userId, Pageable pageable);

    @Query("SELECT count(*) from ApartmentCalendar a WHERE a.user.id = :userId")
    int clientBookingHistoryCount(@Param("userId") int userId);

    List<ApartmentCalendar> findByApartmentIdIn(List<Integer> apartmentsId, Pageable pageable);

    @Query("SELECT count(*) from ApartmentCalendar a WHERE a.apartment.id IN :apartmentsId")
    int ownerRentHistoryCount(@Param("apartmentsId") List<Integer> apartmentsId);

    @Query("SELECT a FROM ApartmentCalendar  a WHERE a.apartment.id = :apartmentId AND a.arrival >= :arrive")
    List<ApartmentCalendar> getFutureBooking(@Param("apartmentId") int apartmentId, @Param("arrive") Date arrive);
}
