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
    @Query("SELECT count(*) from ApartmentCalendar a WHERE a.apartment.id = :apartmentId AND a.arrival = :arrival AND a.departure = :departure")
    int checkDates(@Param("apartmentId") int apartmentId, @Param("arrival") Date arrival, @Param("departure") Date departure);

    @Query("SELECT a from ApartmentCalendar a WHERE a.apartment.id = :apartmentId AND a.departure = :arrival AND a.firstDayFree = true")
    ApartmentCalendar isFirstDayFree(@Param("apartmentId") int apartmentId, @Param("arrival") Date arrival);

    @Query("SELECT a from ApartmentCalendar a WHERE a.apartment.id = :apartmentId AND a.arrival = :departure AND a.lastDayFree = true")
    ApartmentCalendar isLastDayFree(@Param("apartmentId") int apartmentId, @Param("departure") Date departure);

    @Query("SELECT a FROM ApartmentCalendar a WHERE a.apartment.id = :apartmentId AND a.arrival BETWEEN :arrive AND :departure OR a.departure BETWEEN :arrive AND :departure")
    List<ApartmentCalendar> checkBetweenDates(@Param("apartmentId") int apartmentId, @Param("arrive") Date arrive, @Param("departure") Date departure);

    @Query("SELECT SUM(a.currentCountGuest) FROM ApartmentCalendar a WHERE a.apartment.id = :apartmentId AND a.arrival = :arrive AND a.departure = :departure")
    Integer checkGuestsInSharedRoom(@Param("apartmentId") int apartmentId, @Param("arrive") Date arrive, @Param("departure") Date departure);

    List<ApartmentCalendar> findByUserId(int userId, Pageable pageable);

    @Query("SELECT count(*) from ApartmentCalendar a WHERE a.user.id = :userId")
    int clientBookingHistoryCount(@Param("userId") int userId);

    List<ApartmentCalendar> findByApartmentIdIn(List<Integer> apartmentsId, Pageable pageable);
}
