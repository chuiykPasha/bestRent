package rent.form;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class AddAvatarForm {
    @NotEmpty
    private String avatar;
}
