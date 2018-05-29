package rent.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import rent.entities.Role;
import rent.entities.User;
import rent.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class CustomDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);

        if(user == null) {
            throw new UsernameNotFoundException("No user found by email " + email);
        }

        boolean enabled = true;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;

        return new org.springframework.security.core.userdetails.User(user.getEmail(),
                user.getPassword().toLowerCase(), enabled,
                accountNonExpired, credentialsNonExpired, accountNonLocked,
                getAuthorities(user.getRoles()));
    }

    private List<GrantedAuthority> getAuthorities(Set<Role> roles) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for(Role role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.name()));
        }

        return authorities;
    }
}
