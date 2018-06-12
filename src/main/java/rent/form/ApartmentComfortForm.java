package rent.form;

import javax.validation.constraints.NotEmpty;

public class ApartmentComfortForm {
    private Integer id;
    @NotEmpty
    private String name;

    public ApartmentComfortForm(){}

    public ApartmentComfortForm(@NotEmpty String name) {
        this.name = name;
    }

    public ApartmentComfortForm(Integer id, @NotEmpty String name) {
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
