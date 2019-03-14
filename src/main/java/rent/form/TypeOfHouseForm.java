package rent.form;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class TypeOfHouseForm {
    private Integer id;
    @NotEmpty
    private String name;

    public TypeOfHouseForm(Integer id, @NotEmpty String name) {
        this.id = id;
        this.name = name;
    }
}
