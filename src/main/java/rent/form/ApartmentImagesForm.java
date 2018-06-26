package rent.form;

import org.hibernate.annotations.SQLInsert;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class ApartmentImagesForm {
    @Size(min = 1, message = "Minimum one image")
    private List<String> images = new ArrayList<>();

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }
}
