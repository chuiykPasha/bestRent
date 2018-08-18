package rent.form;

import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class ChangeApartmentImagesForm {
    @Size(min = 1, message = "Minimum one image")
    private List<String> images = new ArrayList<>();

    private int apartmentId;

    private List<Double> imagesSize = new ArrayList<>();

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public int getApartmentId() {
        return apartmentId;
    }

    public void setApartmentId(int apartmentId) {
        this.apartmentId = apartmentId;
    }

    public List<Double> getImagesSize() {
        return imagesSize;
    }

    public void setImagesSize(List<Double> imagesSize) {
        this.imagesSize = imagesSize;
    }
}
