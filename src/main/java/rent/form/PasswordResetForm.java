package rent.form;

import lombok.Getter;
import lombok.Setter;
import rent.form.constraint.FieldMatch;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter
@Setter
@FieldMatch(first = "password", second = "confirmPassword", message = "The password fields must match")
public class PasswordResetForm {
    @NotEmpty
    @Size(min = 6, max = 16)
    private String password;

    @NotEmpty
    @Size(min = 6, max = 16)
    private String confirmPassword;

    @NotEmpty
    private String token;
}
