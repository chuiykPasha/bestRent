package rent.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;
import rent.entities.User;
import rent.repository.UserRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CustomDetailsServiceTest {
    @MockBean
    private UserRepository userRepository;
    @Autowired
    private CustomDetailsService userDetailsService;

    @Test
    public void loadUserByUsernameHappyPath(){
        final String EMAIL = "test@gmail.com";
        User user = new User();
        user.setEmail(EMAIL);

        Mockito.when(userRepository.findByEmail(any(String.class))).thenReturn(user);
        UserDetails find = userDetailsService.loadUserByUsername(EMAIL);

        assertNotNull(find);
        assertEquals(EMAIL, find.getUsername());
    }

    @Test(expected = UsernameNotFoundException.class)
    public void loadUserByUsernameException(){
        Mockito.when(userRepository.findByEmail(any(String.class))).thenReturn(null);
        userDetailsService.loadUserByUsername("test@gmail.com");
    }



}