package rent.contoller;

import com.dropbox.core.*;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import com.dropbox.core.v2.sharing.RequestedVisibility;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.dropbox.core.v2.sharing.SharedLinkSettings;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import rent.entities.*;
import rent.form.*;
import rent.model.Mail;
import rent.repository.*;
import rent.service.EmailService;
import rent.service.UploadImageService;

import javax.validation.Valid;
import java.io.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@SessionAttributes(types = {ApartmentInfoForm.class, ApartmentLocationForm.class})
public class ApartmentController {
    @Autowired
    private TypeOfHouseRepository typeOfHouseRepository;
    @Autowired
    private AvailableToGuestRepository availableToGuestRepository;
    @Autowired
    private ApartmentComfortRepository apartmentComfortRepository;
    @Autowired
    private ApartmentRepository apartmentRepository;
    @Autowired
    private ApartmentCalendarRepository apartmentCalendarRepository;
    private final int sizeApartmentsInPage = 9;
    @Autowired
    private UploadImageService uploadImageService;
    @Autowired
    private UserRepository userRepository;
    private final String SHARED_ROOM = "Shared room";
    private final int REMOVE_FIRST_DATE = 0;
    private final int SIZE_HISTORY_IN_PAGE = 5;
    @Autowired
    private EmailService emailService;

    private ApartmentController() {
    }

    @RequestMapping(value = {"/"}, method = RequestMethod.GET)
    public String main(@RequestParam(name = "location", required = false) String location, @RequestParam(name = "page", required = false) Integer page, Model model) {
        int pageNumber = page != null ? page - 1 : 0;

        if(location != null && location != "") {
            int countPage = (int)Math.ceil(apartmentRepository.countPageByLocation(location) / (double)sizeApartmentsInPage);
            model.addAttribute("location", location);
            model.addAttribute("apartments", apartmentRepository.getApartmentsByLocation(location, pageNumber * sizeApartmentsInPage, sizeApartmentsInPage));
            model.addAttribute("countPage", countPage);
            model.addAttribute("current", pageNumber);
        } else {
            int countPage = (int)Math.ceil(apartmentRepository.count() / (double)sizeApartmentsInPage);
            Page<Apartment> apartments = apartmentRepository.findAll(PageRequest.of(pageNumber, sizeApartmentsInPage, Sort.Direction.DESC, "id"));
            model.addAttribute("apartments", apartments.getContent());
            model.addAttribute("countPage", countPage);
            model.addAttribute("current", pageNumber);
            model.addAttribute("location", null);
        }

        return "index";
    }

    @GetMapping("/apartment/{apartment}")
    public String showApartmentById(Apartment apartment, Model model, @AuthenticationPrincipal User user) {
        model.addAttribute("apartment", apartment);
        model.addAttribute("apartmentId", apartment.getId());
        model.addAttribute("defaultAvatar", User.DEFAULT_AVATAR);
        model.addAttribute("availableToGuest", apartment.getAvailableToGuest().getName());
        model.addAttribute("userOnPage", user != null? "userLogin" : "guest");
        model.addAttribute("userId", apartment.getUser().getId());

        List<LocalDate> dates = new ArrayList<>();

        if (!apartment.getAvailableToGuest().getName().equals(SHARED_ROOM)) {
            for (ApartmentCalendar calendar : apartment.getCalendars()) {
                List<LocalDate> datesBetween = getDatesBetween(calendar.getArrival().toLocalDate(), calendar.getDeparture().toLocalDate());
                datesBetween.remove(REMOVE_FIRST_DATE);
                dates.addAll(datesBetween);

                if (!calendar.isFirstDayFree()) {
                    dates.add(calendar.getArrival().toLocalDate());
                }

                if (!calendar.isLastDayFree()) {
                    dates.add(calendar.getDeparture().toLocalDate());
                }
            }
        } else {
            Map<LocalDate, Integer> checkDates = new HashMap<>();

            for(ApartmentCalendar calendar : apartment.getCalendars()){
                List<LocalDate> datesBetween = getDatesBetween(calendar.getArrival().toLocalDate(), calendar.getDeparture().toLocalDate());
                datesBetween.add(calendar.getDeparture().toLocalDate());

                for(LocalDate date : datesBetween) {

                    if(checkDates.containsKey(date)){
                        int countDate = checkDates.get(date).intValue();
                        checkDates.put(date, countDate + calendar.getCurrentCountGuest());
                        continue;
                    }

                    checkDates.put(date, calendar.getCurrentCountGuest());
                }
            }

            //checkLastDates
            for(ApartmentCalendar  lastDay : apartment.getCalendars()){
                int countDate = checkDates.get(lastDay.getDeparture().toLocalDate()).intValue();
                countDate -= lastDay.getCurrentCountGuest();
                checkDates.put(lastDay.getDeparture().toLocalDate(), countDate);
            }


            //fill blocket dates
            for(Map.Entry<LocalDate, Integer> checkDate : checkDates.entrySet()){
                if(checkDate.getValue() == apartment.getMaxNumberOfGuests()){
                    dates.add(checkDate.getKey());
                }
            }
        }

        model.addAttribute("disabledDates", dates);
        return "/apartment/showApartment";
    }

    @GetMapping("/apartment-create-step-one")
    public String fillApartmentInfo(Model model){
        ApartmentInfoForm apartmentInfoForm = new ApartmentInfoForm();
        apartmentInfoForm.setComforts(apartmentComfortRepository.findByIsActiveTrue());
        apartmentInfoForm.setTypeOfHouses(typeOfHouseRepository.findByIsActiveTrue());
        apartmentInfoForm.setAvailableToGuests(availableToGuestRepository.findByIsActiveTrue());
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
                                             SessionStatus sessionStatus,
                                             @AuthenticationPrincipal User user) throws MaxUploadSizeExceededException {
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
                new AvailableToGuest(apartmentInfoForm.getAvailableToGuestId(), null), selectedComforts, apartmentInfoForm.getTitle(), user, apartmentLocationForm.getLongitude(), apartmentLocationForm.getLatitude());

        final int apartmentId = apartmentRepository.save(apartment).getId();

        uploadImageService.uploadApartmentImages(apartmentImagesForm.getImages(), userDetails.getUsername(), apartmentId, apartmentImagesForm.getImagesSize());
        sessionStatus.setComplete();

        while (true) {
            if(UploadImageService.firstImageUploaded == true) {
                break;
            }
        }

        if(!user.getRoles().contains(Role.LANDLORD)) {
            user.getRoles().add(Role.LANDLORD);
            userRepository.save(user);
        }

        addNewUserRoleInSession(Role.LANDLORD);

        return "redirect:/";
    }


    @RequestMapping(value = "/apartment-booking", method = RequestMethod.POST, produces = "text/plain")
    public @ResponseBody String apartmentBooking(@AuthenticationPrincipal User user, @RequestParam String bookingDates, @RequestParam int apartmentId, @RequestParam String availableToGuest, @RequestParam int guestsCount, @RequestParam int maxNumberOfGuests) {
        final int START_DATE = 0;
        final int END_DATE = 1;

        if(bookingDates == null) {
            return "Wrong dates";
        }

        String [] dates = bookingDates.split(" - ");

        if(dates.length != 2) {
            return "Wrong dates";
        }

        if(availableToGuest.equals(SHARED_ROOM)) {
            boolean noPlaces = false;
            List<ApartmentCalendar> testBetweenDates = apartmentCalendarRepository.checkBetweenDates(apartmentId, Date.valueOf(dates[START_DATE]), Date.valueOf(dates[END_DATE]));

            int departureCount = 0;
            int arrivalCOunt = 0;

            for(ApartmentCalendar calendar : testBetweenDates) {
                Date departureDate = calendar.getDeparture();
                if(departureDate.equals(Date.valueOf(dates[START_DATE]))){
                    departureCount += calendar.getCurrentCountGuest();
                }

                if(calendar.getArrival().equals(Date.valueOf(dates[START_DATE]))){
                    arrivalCOunt += calendar.getCurrentCountGuest();
                }
            }

            if((departureCount != 0 && departureCount < guestsCount) && arrivalCOunt < guestsCount){
                return "Sorry no places";
            }

            Map<LocalDate, Integer> checkDates = new HashMap<>();

            for(ApartmentCalendar calendar : testBetweenDates){
                List<LocalDate> datesBetween = getDatesBetween(calendar.getArrival().toLocalDate(), calendar.getDeparture().toLocalDate());
                datesBetween.add(calendar.getDeparture().toLocalDate());

                for(LocalDate date : datesBetween) {
                    if(date.equals(Date.valueOf(dates[START_DATE]).toLocalDate())){
                        continue;
                    }

                    if(checkDates.containsKey(date)){
                        int countDate = checkDates.get(date).intValue();
                        checkDates.put(date, countDate + calendar.getCurrentCountGuest());
                        continue;
                    }

                    checkDates.put(date, calendar.getCurrentCountGuest());
                }
            }

            System.out.println(checkDates);

            List<LocalDate> reservationDates = getDatesBetween(Date.valueOf(dates[START_DATE]).toLocalDate(), Date.valueOf(dates[END_DATE]).toLocalDate());
            reservationDates.add(Date.valueOf(dates[END_DATE]).toLocalDate());


            //fill blocket dates
            for(Map.Entry<LocalDate, Integer> checkDate : checkDates.entrySet()){
                for(LocalDate reservationDate : reservationDates) {
                    if (checkDate.getKey().equals(reservationDate) && checkDate.getValue() == maxNumberOfGuests){
                        noPlaces = true;
                    }
                }
            }

            if(noPlaces){
                return "Sorry no places.";
            }

            ApartmentCalendar apartmentCalendar = new ApartmentCalendar(Date.valueOf(dates[START_DATE]),
                    Date.valueOf(dates[END_DATE]),
                    new Apartment(apartmentId),
                    true,
                    true,
                    guestsCount,
                    user);
            apartmentCalendarRepository.save(apartmentCalendar);
            return "Reservation is successful";
        } else {
            List<ApartmentCalendar> beetwenDates = apartmentCalendarRepository.checkBetweenDates(apartmentId, Date.valueOf(dates[START_DATE]), Date.valueOf(dates[END_DATE]));

            //if(!beetwenDates.isEmpty()){
            //    return "Sorry, these dates are reserved";
            //}

            int count = apartmentCalendarRepository.checkDates(apartmentId,
                    Date.valueOf(dates[START_DATE]),
                    Date.valueOf(dates[END_DATE]));

            if(count == 0) {
                ApartmentCalendar firstDay = apartmentCalendarRepository.isFirstDayFree(apartmentId, Date.valueOf(dates[START_DATE]));

                if(firstDay != null) {

                    firstDay.setFirstDayFree(false);
                    apartmentCalendarRepository.save(firstDay);
                    ApartmentCalendar apartmentCalendar = new ApartmentCalendar(Date.valueOf(dates[START_DATE]),
                            Date.valueOf(dates[END_DATE]),
                            new Apartment(apartmentId),
                            false,
                            true,
                            guestsCount,
                            user);
                    apartmentCalendarRepository.save(apartmentCalendar);
                    return "Reservation is successful";
                }

                ApartmentCalendar lastDay = apartmentCalendarRepository.isFirstDayFree(apartmentId, Date.valueOf(dates[END_DATE]));

                if(lastDay != null){
                    lastDay.setLastDayFree(false);
                    apartmentCalendarRepository.save(lastDay);
                    ApartmentCalendar apartmentCalendar = new ApartmentCalendar(Date.valueOf(dates[START_DATE]),
                            Date.valueOf(dates[END_DATE]),
                            new Apartment(apartmentId),
                            true,
                            false,
                            guestsCount,
                            user);
                    apartmentCalendarRepository.save(apartmentCalendar);
                    return "Reservation is successful";
                }

                ApartmentCalendar apartmentCalendar = new ApartmentCalendar(Date.valueOf(dates[START_DATE]),
                        Date.valueOf(dates[END_DATE]),
                        new Apartment(apartmentId),
                        true,
                        true,
                        guestsCount,
                        user);
                apartmentCalendarRepository.save(apartmentCalendar);
            } else {
                return "Sorry.These dates are not free";
            }
        }

        return "Reservation is successful";
    }

    @GetMapping("my-advertisements")
    public String showMyAdvertisements(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("apartments", apartmentRepository.findByUserIdOrderByIdDesc(user.getId()));
        return "myAdvertisements";
    }

    @GetMapping("owner-rent-history")
    public String ownerRentHistory(@AuthenticationPrincipal User user, @RequestParam(name = "page", required = false) Integer page, Model model){
        int pageNumber = page != null ? page - 1 : 0;
        List<Integer> ownerApartmentsId = user.getApartments().stream().map((s) -> s.getId()).collect(Collectors.toList());
        List<ApartmentCalendar> rent = apartmentCalendarRepository.findByApartmentIdIn(ownerApartmentsId, PageRequest.of(pageNumber, SIZE_HISTORY_IN_PAGE, Sort.Direction.DESC, "id"));
        final int countPage = (int)Math.ceil(apartmentCalendarRepository.ownerRentHistoryCount(ownerApartmentsId) / SIZE_HISTORY_IN_PAGE);
        model.addAttribute("rent", rent);
        model.addAttribute("defaultAvatar", User.DEFAULT_AVATAR);
        model.addAttribute("countPage", countPage);
        model.addAttribute("current", pageNumber);

        return "/apartment/ownerRentHistory";
    }


    @GetMapping("client-booking-history")
    public String clientBookingHistory(@AuthenticationPrincipal User user, @RequestParam(name = "page", required = false) Integer page, Model model){
        final int pageNumber = page != null ? page - 1 : 0;
        final int countPage = (int)Math.ceil(apartmentCalendarRepository.clientBookingHistoryCount(user.getId()) / SIZE_HISTORY_IN_PAGE);
        List<ApartmentCalendar> booking = apartmentCalendarRepository.findByUserId(user.getId(), PageRequest.of(pageNumber, SIZE_HISTORY_IN_PAGE, Sort.Direction.DESC, "id"));
        model.addAttribute("booking", booking);
        model.addAttribute("defaultAvatar", User.DEFAULT_AVATAR);
        model.addAttribute("countPage", countPage);
        model.addAttribute("current", pageNumber);

        return "/apartment/clientBookingHistory";
    }

    @GetMapping("/change-apartment-location/{apartment}")
    public String changeApartmentLocation(Apartment apartment, ChangeApartmentLocationForm changeLocationForm, Model model){
        changeLocationForm.setLocation(apartment.getLocation());
        changeLocationForm.setLatitude(apartment.getLatitude());
        changeLocationForm.setLongitude(apartment.getLongitude());
        changeLocationForm.setApartmentId(apartment.getId());
        model.addAttribute("changeLocationForm", changeLocationForm);
        return "/apartment/changeLocation";
    }

    @PostMapping("/change-apartment-location")
    public String changeApartmentLocationSave(@Valid ChangeApartmentLocationForm changeLocationForm, BindingResult result){
        if(result.hasErrors()) {
            return "/apartment/changeLocation";
        }

        Apartment changeApartment = apartmentRepository.getOne(changeLocationForm.getApartmentId());

        if(!changeApartment.getCalendars().isEmpty()){
            List<ApartmentCalendar> booking = apartmentCalendarRepository.getFutureBooking(changeLocationForm.getApartmentId(), java.sql.Date.valueOf(LocalDate.now()));
            Set<String> uniqueUsersEmail = new HashSet<>();
            for(ApartmentCalendar item : booking) {
                if(uniqueUsersEmail.contains(item.getUser().getEmail())){
                    continue;
                }

                Mail mail = new Mail();
                mail.setFrom("best-rent.tk");
                mail.setSubject("Booking address changed");
                mail.setTo(item.getUser().getEmail());
                Map<String, Object> model = new HashMap<>();
                model.put("from", changeApartment.getLocation());
                model.put("to", changeLocationForm.getLocation());
                model.put("user", changeApartment.getUser());
                model.put("signature", "https://best-rent.tk");
                mail.setModel(model);
                emailService.sendEmail(mail, "email/change-apartment-location");
                uniqueUsersEmail.add(item.getUser().getEmail());
            }
        }

        changeApartment.setLocation(changeLocationForm.getLocation());
        changeApartment.setLatitude(changeLocationForm.getLatitude());
        changeApartment.setLongitude(changeLocationForm.getLongitude());
        apartmentRepository.save(changeApartment);

        return "redirect:/my-advertisements";
    }

    @GetMapping("/change-apartment-images/{apartment}")
    public String changeApartmentImages(Apartment apartment, ChangeApartmentImagesForm changeApartmentImagesForm, Model model) {
        changeApartmentImagesForm.setApartmentId(apartment.getId());
        model.addAttribute("changeApartmentImagesForm", changeApartmentImagesForm);
        model.addAttribute("images", apartment.getImages());
        return "/apartment/changeApartmentImages";
    }

    @PostMapping("/change-apartment-images")
    public String changeApartmentImagesSave(@Valid ChangeApartmentImagesForm changeApartmentImagesForm, BindingResult result, @AuthenticationPrincipal User user, Model model){
        if(changeApartmentImagesForm.getImages().isEmpty()){
            model.addAttribute("changeApartmentImagesForm", changeApartmentImagesForm);
            model.addAttribute("images", apartmentRepository.getOne(changeApartmentImagesForm.getApartmentId()));

            return "/apartment/changeApartmentImages";
        }

        uploadImageService.changeApartmentImages(changeApartmentImagesForm.getImages(), user.getEmail() ,changeApartmentImagesForm.getApartmentId(), changeApartmentImagesForm.getImagesSize());
        return "redirect:/my-advertisements";
    }

    private List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate endDate) {

        long numOfDaysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        return IntStream.iterate(0, i -> i + 1)
                .limit(numOfDaysBetween)
                .mapToObj(i -> startDate.plusDays(i))
                .collect(Collectors.toList());
    }

    private void addNewUserRoleInSession(GrantedAuthority authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<GrantedAuthority> updatedAuthorities = new ArrayList<>(auth.getAuthorities());
        updatedAuthorities.add(authority);
        Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), updatedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }
}

