package rent.form;

import javax.validation.constraints.NotEmpty;

public class AvailableToGuestForm {
    private Integer id;
    @NotEmpty
    private String name;

    public AvailableToGuestForm() { }

    public AvailableToGuestForm(@NotEmpty String name) {
        this.name = name;
    }

    public AvailableToGuestForm(Integer id, @NotEmpty String name) {
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
