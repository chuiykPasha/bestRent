package rent.contoller;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import rent.entities.Apartment;
import rent.entities.AvailableToGuest;
import rent.entities.User;
import rent.repository.ApartmentRepository;
import java.util.Optional;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ApartmentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ApartmentRepository apartmentRepository;


    @Test
    public void main() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    public void showApartmentById() throws Exception {
        Apartment aparment = new Apartment(500);
        aparment.setAvailableToGuest(new AvailableToGuest("all"));
        aparment.setUser(new User("pupkin@gmail.com", "vasya", "pupkin", "111", null));
        aparment.setPrice(100);

        Mockito.when(apartmentRepository.findById(any(Integer.class))).thenReturn(Optional.of(aparment));

        mockMvc.perform(get("/apartment/500"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("apartment", Matchers.hasProperty("id", is(aparment.getId()))));
    }
}