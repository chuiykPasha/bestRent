package rent.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rent.entities.Apartment;

import java.util.List;

public interface ApartmentRepository extends JpaRepository<Apartment, Integer> {

    @Query(value = "SELECT * from apartment WHERE is_active = true AND MATCH (location) AGAINST(:location) LIMIT :start, :end", nativeQuery = true)
    List<Apartment> getApartmentsByLocation(@Param("location") String location, @Param("start") int start, @Param("end") int end);

    @Query(value = "SELECT count(*) FROM apartment WHERE MATCH (location) AGAINST(:location) AND is_active = true", nativeQuery = true)
    int countPageByLocation(@Param("location") String location);

    List<Apartment> findByUserIdAndIsActiveTrueOrderByIdDesc(int userId);

    List<Apartment> findByIsActiveTrue(Pageable pageable);

    @Query(value = "SELECT count(*) FROM apartment WHERE is_active = true", nativeQuery = true)
    int countActiveApartments();
}
