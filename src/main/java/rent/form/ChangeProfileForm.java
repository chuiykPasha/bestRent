package rent.form;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class ChangeProfileForm {
    @NotEmpty
    @Email
    private String email;
    @NotEmpty
    private String name;
    @NotEmpty
    private String surName;

    public ChangeProfileForm(@NotEmpty @Email String email, @NotEmpty String name, @NotEmpty String surName) {
        this.email = email;
        this.name = name;
        this.surName = surName;
    }
}
