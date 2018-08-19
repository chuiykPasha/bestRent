package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rent.entities.Room;

public interface RoomRepository extends JpaRepository<Room, Integer> {
}
