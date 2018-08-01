package rent.form;

import rent.form.constraint.FieldMatch;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

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

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
