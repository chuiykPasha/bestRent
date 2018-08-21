package rent.form;

public class DeleteApartmentForm {
    private int apartmentId;

    public DeleteApartmentForm(int apartmentId) {
        this.apartmentId = apartmentId;
    }

    public int getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(int apartmentId) {
        this.apartmentId = apartmentId;
    }
}
