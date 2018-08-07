package rent.service;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.RateLimitException;
import com.dropbox.core.RetryException;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.UploadErrorException;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.sharing.RequestedVisibility;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.dropbox.core.v2.sharing.SharedLinkSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rent.contoller.ApartmentController;
import rent.entities.Apartment;
import rent.entities.ApartmentImage;
import rent.entities.User;
import rent.repository.ApartmentImageRepository;
import rent.repository.UserRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
public class UploadImageService {
    private final String Token = "5nsmQQ0lxRAAAAAAAAABIXytFyZh8DVGFd3VPIk9KO58T_ZlkoeOcIVxWrhgjH_T";
    private DbxClientV2 client;
    @Autowired
    private ApartmentImageRepository apartmentImageRepository;
    public static boolean firstImageUploaded = false;
    @Autowired
    private UserRepository userRepository;

    public UploadImageService(){
        DbxRequestConfig config = DbxRequestConfig.newBuilder("RentImages")
                .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                .build();
        client = new DbxClientV2(config, Token);
    }

    public String uploadAvatar(String avatar, String userEmail) {
        String[] data = avatar.split(",");
        byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(data[1]);
        InputStream inputStream = new ByteArrayInputStream(imageBytes);
        String fileName = UUID.randomUUID().toString();

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
            } catch (UploadErrorException e) {
                e.printStackTrace();
            } catch (DbxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (isExceptionThrows);

        SharedLinkMetadata slm = null;
        try {
            slm = client.sharing().createSharedLinkWithSettings(filePath, SharedLinkSettings.newBuilder().withRequestedVisibility(RequestedVisibility.PUBLIC).build());
        } catch (DbxException e) {
            e.printStackTrace();
        }
        String url = slm.getUrl();
        User user = userRepository.findByEmail(userEmail);

        if(user.getAvatarUrl() != null) {
            try {
                client.files().deleteV2(user.getAvatarPath());
            } catch (DbxException e) {
                e.printStackTrace();
            }
        }
        
        user.setAvatarPath(filePath);
        user.setAvatarUrl(getDlUriToDropBoxImage(url));
        userRepository.save(user);
        return user.getAvatarUrl();
    }

    public void uploadApartmentImages(List<String> images, String userName, int apartmentId) {
        for (int i = 0; i < images.size(); i++) {
            String[] data = images.get(i).split(",");
            String img;

            if (data.length == 2) {
                img = data[1];
            } else {
                img = images.get(1);
                new Thread(new UploadImage(img, userName, apartmentId)).start();
                break;
            }

            new Thread(new UploadImage(img, userName, apartmentId)).start();
        }
    }

    public class UploadImage implements Runnable {
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
                firstImageUploaded = true;
            } catch (DbxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getDlUriToDropBoxImage(String oldUrl) {
        return oldUrl.replace("www.dropbox.com", "dl.dropboxusercontent.com").replace("?dl=0", "");
    }
}
