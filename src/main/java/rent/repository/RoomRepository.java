package rent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rent.entities.Room;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> getAllRoomsByApartmentId(int apartmentId);
}
