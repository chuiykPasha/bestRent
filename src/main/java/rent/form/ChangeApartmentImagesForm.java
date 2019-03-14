package rent.form;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ChangeApartmentImagesForm {
    @Size(min = 1, message = "Minimum one image")
    private List<String> images = new ArrayList<>();

    private int apartmentId;

    private List<Double> imagesSize = new ArrayList<>();
}
