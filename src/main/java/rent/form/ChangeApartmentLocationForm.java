package rent.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class ChangeApartmentLocationForm {
    @NotEmpty
    private String location;

    private double latitude;

    private double longitude;

    private int apartmentId;
}
