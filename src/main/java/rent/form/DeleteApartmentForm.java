package rent.form;

import lombok.Getter;

@Getter
public class DeleteApartmentForm {
    private int apartmentId;

    public DeleteApartmentForm(int apartmentId) {
        this.apartmentId = apartmentId;
    }
}
