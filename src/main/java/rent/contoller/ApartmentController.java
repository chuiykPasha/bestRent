package rent.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import rent.dto.BookingDto;
import rent.dto.MailDto;
import rent.entities.*;
import rent.form.*;
import rent.repository.*;
import rent.service.BookingService;
import rent.service.EmailService;
import rent.service.UploadImageService;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
    private final String ENTIRE_ROOM = "Entire apartment";
    private final String PRIVATE_ROOM = "Private room";
    private final int REMOVE_FIRST_DATE = 0;
    private final int SIZE_HISTORY_IN_PAGE = 5;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private BookingService bookingService;

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
            int countPage = (int)Math.ceil(apartmentRepository.countActiveApartments() / (double)sizeApartmentsInPage);
            List<Apartment> apartments = apartmentRepository.findByIsActiveTrue(PageRequest.of(pageNumber, sizeApartmentsInPage, Sort.Direction.DESC, "id"));
            model.addAttribute("apartments", apartments);
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
        model.addAttribute("price", apartment.getPrice());

        List<LocalDate> dates = new ArrayList<>();

        if (apartment.getAvailableToGuest().getName().equals(ENTIRE_ROOM)) {
            dates = bookingService.getBlockedDatesInEntireApartment(apartment.getCalendars());
        } else if(apartment.getAvailableToGuest().getName().equals(SHARED_ROOM)){
            dates = bookingService.getBlockedDatesInSharedRoom(apartment.getCalendars(), apartment.getMaxNumberOfGuests());
        } else if(apartment.getAvailableToGuest().getName().equals(PRIVATE_ROOM)){
            dates = bookingService.getBlockedDatesInPrivateRoom(apartment.getCalendars(), apartment.getRooms().size());
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
        model.addAttribute("privateRoomId", availableToGuestRepository.findByNameAndIsActiveTrue("Private room").getId());

        return "/apartment/createStepOne";
    }

    @PostMapping("/apartment-create-step-one")
    public String moveStepTwo(@Valid ApartmentInfoForm apartmentInfoForm, BindingResult result, Model model) {
        if(result.hasErrors()) {
            return "/apartment/createStepOne";
        }

        int privateRoomId = availableToGuestRepository.findByNameAndIsActiveTrue("Private room").getId();

        if(apartmentInfoForm.getNumberOfRooms() > apartmentInfoForm.getMaxNumberOfGuests()){
            result.rejectValue("maxNumberOfGuests", null, "Check the maximum number of guests");
            model.addAttribute("privateRoomId", privateRoomId);
            return "/apartment/createStepOne";
        }

        if(apartmentInfoForm.getAvailableToGuestId() == privateRoomId){
            int countGuestsInRooms = apartmentInfoForm.getGuestsInRoom().stream().mapToInt(i -> i).sum();

            if(countGuestsInRooms != apartmentInfoForm.getMaxNumberOfGuests()){
                result.rejectValue("guestsInRoom", null, "Maximum number of guests and guests in the room.");
                model.addAttribute("privateRoomId", privateRoomId);
                return "/apartment/createStepOne";
            }
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
        Set<ApartmentComfort> selectedComforts = getSelectedComforts(apartmentInfoForm.getSelectedComforts());

        Apartment apartment = new Apartment(apartmentInfoForm.getDescription(), apartmentLocationForm.getLocation(), apartmentInfoForm.getPrice().floatValue(),
                apartmentInfoForm.getMaxNumberOfGuests(),
                typeOfHouseRepository.getOne(apartmentInfoForm.getTypeOfHouseId()),
                availableToGuestRepository.getOne(apartmentInfoForm.getAvailableToGuestId()), selectedComforts, apartmentInfoForm.getTitle(), user,
                    apartmentLocationForm.getLongitude(), apartmentLocationForm.getLatitude(), apartmentInfoForm.getNumberOfRooms());

        final int apartmentId = apartmentRepository.save(apartment).getId();

        uploadImageService.uploadApartmentImages(apartmentImagesForm.getImages(), userDetails.getUsername(), apartmentId, apartmentImagesForm.getImagesSize());
        sessionStatus.setComplete();
        updateUserRole(user);
        waitForUploadFirstImg();
        saveApartmentRooms(apartmentInfoForm, apartmentId);

        return "redirect:/";
    }

    private Set<ApartmentComfort> getSelectedComforts(List<Integer> selectedComforts){
        Set<ApartmentComfort> result = new HashSet<>();

        for(int selected : selectedComforts) {
            result.add(apartmentComfortRepository.getOne(selected));
        }

        return result;
    }

    private void waitForUploadFirstImg(){
        while (true) {
            if(UploadImageService.firstImageUploaded) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void updateUserRole(User user){
        if(!user.getRoles().contains(Role.LANDLORD)) {
            user.getRoles().add(Role.LANDLORD);
            userRepository.save(user);
        }

        addNewUserRoleInSession(Role.LANDLORD);
    }

    private void saveApartmentRooms(ApartmentInfoForm apartmentInfoForm, int apartmentId){
        if(apartmentInfoForm.getAvailableToGuestId() == availableToGuestRepository.findByNameAndIsActiveTrue("Private room").getId()){
            List<Room> rooms = new ArrayList<>();

            for(int guestsInRoom: apartmentInfoForm.getGuestsInRoom()){
                rooms.add(new Room(guestsInRoom, new Apartment(apartmentId)));
            }

            roomRepository.saveAll(rooms);
        }
    }


    @RequestMapping(value = "/apartment-booking", method = RequestMethod.POST)
    public @ResponseBody
    BookingDto apartmentBooking(@AuthenticationPrincipal User user, @RequestParam String bookingDates, @RequestParam int apartmentId,
                                @RequestParam String availableToGuest, @RequestParam int guestsCount, @RequestParam int maxNumberOfGuests,
                                @RequestParam float price, @RequestParam boolean approve) {
        final int START_DATE = 0;
        final int END_DATE = 1;

        if(bookingDates == null) {
            return new BookingDto("Wrong dates");
        }

        String [] dates = bookingDates.split(" - ");

        if(dates.length != 2) {
            return new BookingDto("Wrong dates");
        }

        final Date startDate = Date.valueOf(dates[START_DATE]);
        final Date endDate = Date.valueOf(dates[END_DATE]);

        if(availableToGuest.equals(ENTIRE_ROOM)) {
           return bookingService.bookingEntireApartment(apartmentId, startDate, endDate, guestsCount, user, price);
        } else if (availableToGuest.equals(SHARED_ROOM)) {
            return bookingService.bookingSharedRoom(apartmentId, startDate, endDate, guestsCount, user, price, maxNumberOfGuests);
        } else if(availableToGuest.equals(PRIVATE_ROOM)){
            Apartment apartment = apartmentRepository.getOne(apartmentId);

            //check if apartment have equals room
            if(bookingService.apartmentHaveRoomWhereMaxGuestsEqualsGuests(apartment.getRooms(), guestsCount)){
                for(Room room : apartment.getRooms()){
                    if(guestsCount <= room.getMaxNumberOfGuests()){
                        List<ApartmentCalendar> betweenDates = apartmentCalendarRepository.checkBetweenDatesPrivateRoom(apartmentId, startDate, endDate, room.getId());

                        if(betweenDates.isEmpty()){
                            apartmentCalendarRepository.save(new ApartmentCalendar(startDate, endDate, true, true,
                                    guestsCount, apartment, user, room, false, price));
                            return new BookingDto(bookingService.getBlockedDatesInPrivateRoom(apartment.getCalendars(), apartment.getRooms().size()), "Reservation is successful");
                        }

                        if(betweenDates.size() == 1){
                            if(betweenDates.get(0).isFirstDayFree() && betweenDates.get(0).getArrival().equals(endDate)){
                                betweenDates.get(0).setFirstDayFree(false);
                                apartmentCalendarRepository.save(betweenDates.get(0));
                                apartmentCalendarRepository.save(new ApartmentCalendar(startDate, endDate, true, false,
                                        guestsCount, apartment, user, room, false, price));
                                return new BookingDto(bookingService.getBlockedDatesInPrivateRoom(apartment.getCalendars(), apartment.getRooms().size()), "Reservation is successful");
                            } else if(betweenDates.get(0).isLastDayFree() && betweenDates.get(0).getDeparture().equals(startDate)){
                                betweenDates.get(0).setLastDayFree(false);
                                apartmentCalendarRepository.save(betweenDates.get(0));
                                apartmentCalendarRepository.save(new ApartmentCalendar(startDate, endDate, false, true,
                                        guestsCount, apartment, user, room, false, price));
                                return new BookingDto(bookingService.getBlockedDatesInPrivateRoom(apartment.getCalendars(), apartment.getRooms().size()), "Reservation is successful");
                            }
                        }else if(betweenDates.size() == 2 && (betweenDates.get(0).getDeparture().equals(startDate) && betweenDates.get(0).isLastDayFree()) &&
                                (betweenDates.get(1).isFirstDayFree() && betweenDates.get(1).getArrival().equals(endDate)) ||
                                betweenDates.size() == 2 && (betweenDates.get(1).getDeparture().equals(startDate) && betweenDates.get(1).isLastDayFree()) &&
                                        (betweenDates.get(0).isFirstDayFree() && betweenDates.get(0).getArrival().equals(endDate))){
                            betweenDates.get(0).setLastDayFree(false);
                            apartmentCalendarRepository.save(betweenDates.get(0));
                            betweenDates.get(1).setFirstDayFree(false);
                            apartmentCalendarRepository.save(betweenDates.get(1));
                            apartmentCalendarRepository.save(new ApartmentCalendar(startDate, endDate, false, false,
                                    guestsCount, apartment, user, room, false, price));

                            return new BookingDto(bookingService.getBlockedDatesInPrivateRoom(apartment.getCalendars(), apartment.getRooms().size()), "Reservation is successful");
                        }
                    }
                }
            } else {
                //for find more one room and check date
                List<ApartmentCalendar> newCalendars = new ArrayList<>();
                List<ApartmentCalendar> changedCalendars = new ArrayList<>();
                int guestsInReservedRoom = 0;
                int guests = guestsCount;

                for(Room room : apartment.getRooms()){
                    if(guestsInReservedRoom >= guestsCount){
                        break;
                    }

                    guests -= room.getMaxNumberOfGuests();
                    int guestSopped = guests >= 0 ? room.getMaxNumberOfGuests() : room.getMaxNumberOfGuests() - (guests * -1);

                    List<ApartmentCalendar> betweenDates = apartmentCalendarRepository.checkBetweenDatesPrivateRoom(apartmentId, startDate, endDate, room.getId());

                    if(betweenDates.isEmpty()){
                        newCalendars.add(new ApartmentCalendar(startDate, endDate, true, true,
                                guestSopped, apartment, user, room, false, price));
                        guestsInReservedRoom += room.getMaxNumberOfGuests();
                    } else if(betweenDates.size() == 1){
                        if(betweenDates.get(0).isFirstDayFree() && betweenDates.get(0).getArrival().equals(endDate)){
                            betweenDates.get(0).setFirstDayFree(false);
                            changedCalendars.add(betweenDates.get(0));
                            newCalendars.add(new ApartmentCalendar(startDate, endDate, true, false,
                                    guestSopped, apartment, user, room, false, price));
                            guestsInReservedRoom += room.getMaxNumberOfGuests();
                        } else if(betweenDates.get(0).isLastDayFree() && betweenDates.get(0).getDeparture().equals(startDate)){
                            betweenDates.get(0).setLastDayFree(false);
                            changedCalendars.add(betweenDates.get(0));
                            newCalendars.add(new ApartmentCalendar(startDate, endDate, false, true,
                                    guestSopped, apartment, user, room, false, price));
                            guestsInReservedRoom += room.getMaxNumberOfGuests();
                        }
                    }else if(betweenDates.size() == 2 && (betweenDates.get(0).getDeparture().equals(startDate) && betweenDates.get(0).isLastDayFree()) &&
                            (betweenDates.get(1).isFirstDayFree() && betweenDates.get(1).getArrival().equals(endDate)) ||
                            betweenDates.size() == 2 && (betweenDates.get(1).getDeparture().equals(startDate) && betweenDates.get(1).isLastDayFree()) &&
                                    (betweenDates.get(0).isFirstDayFree() && betweenDates.get(0).getArrival().equals(endDate))){
                        betweenDates.get(0).setLastDayFree(false);
                        changedCalendars.add(betweenDates.get(0));
                        betweenDates.get(1).setFirstDayFree(false);
                        changedCalendars.add(betweenDates.get(1));
                        newCalendars.add(new ApartmentCalendar(startDate, endDate, false, false,
                                guestSopped, apartment, user, room, false, price));
                        guestsInReservedRoom += room.getMaxNumberOfGuests();
                    }
                }

                if(guestsInReservedRoom < guestsCount){
                    return new BookingDto("Sorry don't have rooms for your guests count");
                }

                if(approve){
                    if(!changedCalendars.isEmpty()) {
                        apartmentCalendarRepository.saveAll(changedCalendars);
                    }
                    apartmentCalendarRepository.saveAll(newCalendars);

                    return new BookingDto(bookingService.getBlockedDatesInPrivateRoom(apartment.getCalendars(), apartment.getRooms().size()), "Reservation is successful");
                }

                return new BookingDto("For your number of guests you will need " + newCalendars.size() + " rooms and the price will be " + price * newCalendars.size() + " . Will you booking?");
            }

            return new BookingDto("Sorry these dates reserved");
        }

        return new BookingDto("ERROR");
    }

    @GetMapping("my-advertisements")
    public String showMyAdvertisements(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("apartments", apartmentRepository.findByUserIdAndIsActiveTrueOrderByIdDesc(user.getId()));
        return "myAdvertisements";
    }

    @GetMapping("owner-rent-history")
    public String ownerRentHistory(@AuthenticationPrincipal User user, @RequestParam(name = "page", required = false) Integer page, Model model){
        int pageNumber = page != null ? page - 1 : 0;
        List<Integer> ownerApartmentsId = user.getApartments().stream().map((s) -> s.getId()).collect(Collectors.toList());
        List<ApartmentCalendar> rent = apartmentCalendarRepository.findByApartmentIdIn(ownerApartmentsId, PageRequest.of(pageNumber, SIZE_HISTORY_IN_PAGE, Sort.Direction.DESC, "id"));
        final int countPage = (int)Math.ceil(apartmentCalendarRepository.ownerRentHistoryCount(ownerApartmentsId) / (double)SIZE_HISTORY_IN_PAGE);
        model.addAttribute("rent", rent);
        model.addAttribute("defaultAvatar", User.DEFAULT_AVATAR);
        model.addAttribute("countPage", countPage);
        model.addAttribute("current", pageNumber);

        return "/apartment/ownerRentHistory";
    }


    @GetMapping("client-booking-history")
    public String clientBookingHistory(@AuthenticationPrincipal User user, @RequestParam(name = "page", required = false) Integer page, Model model){
        final int pageNumber = page != null ? page - 1 : 0;
        final int countPage = (int)Math.ceil(apartmentCalendarRepository.clientBookingHistoryCount(user.getId()) / (double)SIZE_HISTORY_IN_PAGE);
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

                MailDto mail = new MailDto();
                mail.setFrom("best-rent.tk");
                mail.setSubject("BookingDto address changed");
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

    @GetMapping("/change-apartment-info/{apartment}")
    public String changeApartmentInfo(Apartment apartment, Model model){
        ChangeApartmentInfoForm changeApartmentInfoForm = new ChangeApartmentInfoForm(apartment.getId(), apartment.getDescription(), BigDecimal.valueOf(apartment.getPrice()),
                apartment.getMaxNumberOfGuests(), apartment.getTitle() ,apartmentComfortRepository.findByIsActiveTrue(), apartment.getNumberOfRooms());

        model.addAttribute("changeApartmentInfoForm", changeApartmentInfoForm);
        model.addAttribute("selectedChosen", apartment.getApartmentComforts());

        if(apartment.getAvailableToGuest().getName().equals("Private room")) {
            model.addAttribute("guestsInRooms", apartment.getRooms());
            model.addAttribute("availableToGuests", "Private room");
        }

        return "/apartment/changeApartmentInfo";
    }

    @PostMapping("/change-apartment-info")
    public String changeApartmentInfoSave(@Valid ChangeApartmentInfoForm changeApartmentInfoForm, BindingResult bindingResult, Model model){
        if(bindingResult.hasErrors()){
            return "/apartment/changeApartmentInfo";
        }

        Apartment apartment = apartmentRepository.findById(changeApartmentInfoForm.getId()).get();

        if(apartment.getAvailableToGuest().getName().equals("Private room") && changeApartmentInfoForm.getMaxNumberOfGuests() != apartment.getMaxNumberOfGuests()){
            List<Room> rooms = roomRepository.findByApartmentId(changeApartmentInfoForm.getId());

            for(int i = 0; i < changeApartmentInfoForm.getGuestsInRoom().size(); i++){
                rooms.get(i).setMaxNumberOfGuests(changeApartmentInfoForm.getGuestsInRoom().get(i));
            }

            roomRepository.saveAll(rooms);
        }

        apartment.setMaxNumberOfGuests(changeApartmentInfoForm.getMaxNumberOfGuests());
        apartment.setTitle(changeApartmentInfoForm.getTitle());
        apartment.setDescription(changeApartmentInfoForm.getDescription());
        apartment.setPrice(changeApartmentInfoForm.getPrice().floatValue());

        apartment.getApartmentComforts().clear();
        Set<ApartmentComfort> comforts = new HashSet<>();

        for(int comfort : changeApartmentInfoForm.getSelectedComforts()){
            comforts.add(new ApartmentComfort(comfort));
        }

        apartment.setApartmentComforts(comforts);
        apartmentRepository.save(apartment);

        return "redirect:/my-advertisements";
    }

    @GetMapping("/delete-apartment/{apartment}")
    public String deleteApartment(Apartment apartment, Model model){
        DeleteApartmentForm deleteApartmentForm = new DeleteApartmentForm(apartment.getId());
        model.addAttribute("apartment", apartment);
        model.addAttribute("deleteApartmentForm", deleteApartmentForm);

        return "/apartment/deleteApartment";
    }

    @PostMapping("/delete-apartment")
    public String deleteApartmentConfirm(DeleteApartmentForm deleteApartmentForm){
        Apartment apartment = apartmentRepository.findById(deleteApartmentForm.getApartmentId()).get();

        if(apartment == null){
            return "redirect:/my-advertisements";
        }

        Set<String> emailsUsers = new HashSet<>();
        Set<ApartmentCalendar> canceled = new HashSet<>();

        for(ApartmentCalendar apartmentCalendar : apartment.getCalendars()){
            if(!emailsUsers.contains(apartmentCalendar.getUser().getEmail())){
                emailsUsers.add(apartmentCalendar.getUser().getEmail());
                canceled.add(apartmentCalendar);
            }

            apartmentCalendar.setCanceled(true);
        }

        Thread sendEmails = new Thread(){
            public void run(){
                for(ApartmentCalendar apartmentCalendar : canceled) {
                    MailDto mail = new MailDto();
                    mail.setFrom("best-rent.tk");
                    mail.setSubject("Your reservation has been canceled");
                    mail.setTo(apartmentCalendar.getUser().getEmail());
                    Map<String, Object> model = new HashMap<>();
                    model.put("address", apartmentCalendar.getApartment().getLocation());
                    model.put("user", apartmentCalendar.getUser());
                    model.put("signature", "https://best-rent.tk");
                    mail.setModel(model);
                    emailService.sendEmail(mail, "email/cancel-reservation");
                }
            }
        };

        sendEmails.start();

        apartment.setActive(false);
        apartmentRepository.save(apartment);

        return "redirect:/my-advertisements";
    }

    private void addNewUserRoleInSession(GrantedAuthority authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<GrantedAuthority> updatedAuthorities = new ArrayList<>(auth.getAuthorities());
        updatedAuthorities.add(authority);
        Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), updatedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }
}

