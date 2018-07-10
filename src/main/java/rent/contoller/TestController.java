package rent.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import rent.entities.Apartment;
import rent.entities.ApartmentCalendar;
import rent.entities.ApartmentImage;
import rent.entities.User;
import rent.repository.ApartmentRepository;

import java.io.Serializable;
import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class TestController {
    @Autowired
    private ApartmentRepository apartmentRepository;

    @RequestMapping(value = "/apartment-get-reservation-dates/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public TestResponse getReservationDatesByApartmentId(@PathVariable("id") int id) {
        return new TestResponse(apartmentRepository.getOne(id).getImages());
    }

    private class TestResponse implements Serializable {
        private Set<ApartmentImage> images;

        public TestResponse(Set<ApartmentImage> images) {
            this.images = images;
        }

        public Set<ApartmentImage> getImages() {
            return images;
        }

        public void setImages(Set<ApartmentImage> images) {
            this.images = images;
        }
    }
}
