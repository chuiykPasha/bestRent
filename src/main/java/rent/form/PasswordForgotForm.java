package rent.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class PasswordForgotForm {
    @Email
    @NotEmpty
    private String email;
}
