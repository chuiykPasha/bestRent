package rent.form;

import rent.form.constraint.FieldMatch;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
@FieldMatch(first = "password", second = "confirmPassword", message = "The password fields must match")
public class RegisterForm {
    @Email
    @NotEmpty
    private String email;
    @NotEmpty
    @Min(6)
    private String password;

    @NotEmpty
    @Min(6)
    private String confirmPassword;

    @NotEmpty
    private String name;
    @NotEmpty
    private String surName;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
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
