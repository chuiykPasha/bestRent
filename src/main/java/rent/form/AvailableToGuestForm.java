package rent.form;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Getter
@Setter
public class AvailableToGuestForm implements Serializable{
    private Integer id;

    @NotEmpty
    private String name;

    public AvailableToGuestForm(Integer id, @NotEmpty String name) {
        this.id = id;
        this.name = name;
    }
}
