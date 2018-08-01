package rent.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }
}
