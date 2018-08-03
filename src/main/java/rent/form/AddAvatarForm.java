package rent.form;

import javax.validation.constraints.NotEmpty;

public class AddAvatarForm {
    @NotEmpty
    private String avatar;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
