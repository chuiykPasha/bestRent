package rent.contoller;

import com.dropbox.core.*;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import com.dropbox.core.v2.sharing.RequestedVisibility;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.dropbox.core.v2.sharing.SharedLinkSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import rent.entities.*;
import rent.form.ApartmentImagesForm;
import rent.form.ApartmentInfoForm;
import rent.form.ApartmentLocationForm;
import rent.repository.*;
import rent.service.FullTextSearchService;

import javax.validation.Valid;
import java.io.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Controller
@SessionAttributes(types = {ApartmentInfoForm.class, ApartmentLocationForm.class})
public class ApartmentController {
    private final String token = "5nsmQQ0lxRAAAAAAAAABIXytFyZh8DVGFd3VPIk9KO58T_ZlkoeOcIVxWrhgjH_T";
    private DbxClientV2 client;
    @Autowired
    private TypeOfHouseRepository typeOfHouseRepository;
    @Autowired
    private AvailableToGuestRepository availableToGuestRepository;
    @Autowired
    private ApartmentComfortRepository apartmentComfortRepository;
    @Autowired
    private ApartmentRepository apartmentRepository;
    private List<ApartmentImage> images = new CopyOnWriteArrayList<>();
    @Autowired
    private ApartmentImageRepository apartmentImageRepository;
    @Autowired
    private FullTextSearchService fullTextSearchService;

    private ApartmentController() {
        DbxRequestConfig config = DbxRequestConfig.newBuilder("RentImages")
                .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                .build();
        client = new DbxClientV2(config, token);
    }

    @RequestMapping(value = {"/"}, method = RequestMethod.GET)
    public String main(@RequestParam(name = "location", required = false) String location, Model model) {
        if(location != null && location != "") {
            model.addAttribute("location", location);
            model.addAttribute("apartments", fullTextSearchService.searchApartmentByLocation(location));
        } else {
            model.addAttribute("apartments", apartmentRepository.findAll());
        }

        return "index";
    }

    @GetMapping("/apartment-create-step-one")
    public String fillApartmentInfo(Model model){
        ApartmentInfoForm apartmentInfoForm = new ApartmentInfoForm();
        apartmentInfoForm.setComforts(apartmentComfortRepository.findAll());
        apartmentInfoForm.setTypeOfHouses(typeOfHouseRepository.findAll());
        apartmentInfoForm.setAvailableToGuests(availableToGuestRepository.findAll());
        model.addAttribute("apartmentInfoForm", apartmentInfoForm);

        return "/apartment/createStepOne";
    }

    @PostMapping("/apartment-create-step-one")
    public String moveStepTwo(@Valid ApartmentInfoForm apartmentInfoForm, BindingResult result) {
        if(result.hasErrors()) {
            return "/apartment/createStepOne";
        }

        return "redirect:/apartment-create-step-two";
    }

    @GetMapping("/apartment-create-step-two")
    public String fillApartmentLocation(ApartmentLocationForm apartmentLocationForm) {
        return "/apartment/createStepTwo";
    }

    @PostMapping("/apartment-create-step-two")
    public String moveStepThree(@Valid @ModelAttribute ApartmentLocationForm apartmentLocationForm, BindingResult result) {
        if(result.hasErrors()) {
            return "/apartment/createStepTwo";
        }

        return "redirect:/apartment-create-step-three";
    }

    @GetMapping("/apartment-create-step-three")
    public String fillApartmentPhoto(ApartmentImagesForm apartmentImagesForm) {
        return "/apartment/createStepThree";
    }

    @PostMapping("/apartment-create-step-three")
    public String saveApartmentAdvertisement(@Valid ApartmentImagesForm apartmentImagesForm,
                                             BindingResult result,
                                             ApartmentInfoForm apartmentInfoForm,
                                             ApartmentLocationForm apartmentLocationForm,
                                             SessionStatus sessionStatus) throws MaxUploadSizeExceededException {
        if(result.hasErrors()) {
            return "/apartment/createStepThree";
        }

        final UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Set<ApartmentComfort> selectedComforts = new HashSet<>();

        for(int selected : apartmentInfoForm.getSelectedComforts()) {
            selectedComforts.add(new ApartmentComfort(selected));
        }

        Apartment apartment = new Apartment(apartmentInfoForm.getDescription(), apartmentLocationForm.getLocation(), apartmentInfoForm.getPrice().floatValue(),
                apartmentInfoForm.getMaxNumberOfGuests(),
                new TypeOfHouse(apartmentInfoForm.getTypeOfHouseId(), null),
                new AvailableToGuest(apartmentInfoForm.getAvailableToGuestId(), null), selectedComforts, apartmentInfoForm.getTitle());

        final int apartmentId = apartmentRepository.save(apartment).getId();

        Runnable myRunnable = new Runnable() {

            public void run() {
                for (int i = 0; i < apartmentImagesForm.getImages().size(); i++) {
                    String [] data = apartmentImagesForm.getImages().get(i).split(",");
                    String img;

                    if(data.length == 2) {
                        img = data[1];
                    } else {
                        img = apartmentImagesForm.getImages().get(1);
                        new Thread(new UploadImage(img, userDetails.getUsername(), apartmentId)).start();
                        break;
                    }

                    new Thread(new UploadImage(img, userDetails.getUsername(), apartmentId)).start();
                }
            }
        };

        new Thread(myRunnable).start();
        sessionStatus.setComplete();
        return "redirect:/";
    }

    class UploadImage implements Runnable {
        private String imgBase64;
        private String userEmail;
        private int apartmentId;

        UploadImage(String imgBase64, String userEmail, int apartmentId) {
            this.imgBase64 = imgBase64;
            this.userEmail = userEmail;
            this.apartmentId = apartmentId;
        }

        public void run() {
            byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(imgBase64);
            InputStream inputStream = new ByteArrayInputStream(imageBytes);
            String fileName = UUID.randomUUID().toString();
            try {
                String filePath = "/" + userEmail + "/" + fileName + ".jpg";
                boolean isExceptionThrows;
                do {
                    isExceptionThrows = false;

                    try {
                        client.files().uploadBuilder(filePath).withMode(WriteMode.ADD).uploadAndFinish(inputStream);
                    } catch (RateLimitException exception) {
                        isExceptionThrows = true;
                    } catch (RetryException exception) {
                        isExceptionThrows = true;
                    }
                } while (isExceptionThrows);

                SharedLinkMetadata slm = client.sharing().createSharedLinkWithSettings(filePath, SharedLinkSettings.newBuilder().withRequestedVisibility(RequestedVisibility.PUBLIC).build());
                String url = slm.getUrl();
                apartmentImageRepository.save(new ApartmentImage(filePath, getDlUriToDropBoxImage(url), new Apartment(apartmentId)));
            } catch (DbxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String getDlUriToDropBoxImage(String oldUrl) {
            return oldUrl.replace("www.dropbox.com", "dl.dropboxusercontent.com").replace("?dl=0", "");
        }
    }
}

