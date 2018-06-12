package rent.form;

import javax.validation.constraints.NotEmpty;

public class TypeOfHouseForm {
    private Integer id;
    @NotEmpty
    private String name;

    public TypeOfHouseForm(){}

    public TypeOfHouseForm(@NotEmpty String name) {
        this.name = name;
    }

    public TypeOfHouseForm(Integer id, @NotEmpty String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
