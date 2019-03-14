package rent.form;

import lombok.Getter;
import lombok.Setter;
import rent.form.constraint.FieldMatch;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter
@Setter
@FieldMatch(first = "newPassword", second = "confirmPassword", message = "The password fields must match")
public class ChangePasswordForm {
    @NotEmpty
    @Size(min = 6)
    private String oldPassword;

    @NotEmpty
    @Size(min = 6)
    private String newPassword;

    @NotEmpty
    @Size(min = 6)
    private String confirmPassword;
}
