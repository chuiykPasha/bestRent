package rent.entities;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    USER, ADMIN, LANDLORD;

    @Override
    public String getAuthority() {
        return name();
    }
}
