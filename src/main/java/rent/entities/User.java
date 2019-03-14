package rent.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user")
public class User implements UserDetails {
    @Transient
    //public static final String DEFAULT_AVATAR = "https://www.dl.dropboxusercontent.com/s/5o7j3wapxg8w359/no_avatar.jpg";
    public static final String DEFAULT_AVATAR = "/css/no_avatar.jpg";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String email;
    private String name;
    private String surName;
    private String password;
    private String avatarPath;
    private String avatarUrl;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "userRole", joinColumns = @JoinColumn(name = "userId"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

    @OneToMany(mappedBy = "user")
    private Set<Apartment> apartments = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<ApartmentCalendar> booking = new HashSet<>();

    public User(String email, String name, String surName, String password, Set<Role> roles) {
        this.email = email;
        this.name = name;
        this.surName = surName;
        this.password = password;
        this.roles = roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getRoles();
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
