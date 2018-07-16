package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rent.entities.ApartmentCalendar;

public interface ApartmentCalendarRepository extends JpaRepository<ApartmentCalendar, Integer> {
}
