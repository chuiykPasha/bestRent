package rent.form;

import javax.validation.constraints.NotEmpty;

public class ApartmentLocationForm {
    @NotEmpty
    private String location;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
