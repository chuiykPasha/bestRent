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
import org.apache.tomcat.jni.Time;
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
import java.util.*;

@Service
public class UploadImageService {
    @Autowired
    private DbxClientV2 client;
    @Autowired
    private ApartmentImageRepository apartmentImageRepository;
    @Autowired
    private UserRepository userRepository;
    public volatile static boolean firstImageUploaded = false;

    public String uploadAvatar(String avatar, String userEmail) {
        String fileName = UUID.randomUUID().toString();
        String filePath = "/" + userEmail + "/" + fileName + ".jpg";
        uploadUserAvatar(filePath, convertBase64ImagToInputStream(avatar));
        User user = userRepository.findByEmail(userEmail);
        deleteUserAvatar(user.getAvatarPath());
        user.setAvatarPath(filePath);
        user.setAvatarUrl(getUserAvatarUrl(filePath));
        return user.getAvatarUrl();
    }

    private InputStream convertBase64ImagToInputStream(String img){
        String[] data = img.split(",");
        byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(data[1]);
        return new ByteArrayInputStream(imageBytes);
    }

    private void uploadUserAvatar(String filePath, InputStream inputStream){
        boolean isExceptionThrows = false;
        do {
            try {
                client.files().uploadBuilder(filePath).withMode(WriteMode.ADD).uploadAndFinish(inputStream);
            } catch (Exception ex) {
                isExceptionThrows = true;
            }
        } while (isExceptionThrows);
    }

    private void deleteUserAvatar(String path){
        if(path != null){
            try {
                client.files().deleteV2(path);
            } catch (DbxException e) {
                e.printStackTrace();
            }
        }
    }

    private String getUserAvatarUrl(String filePath){
        SharedLinkMetadata sharedLink = null;
        try {
            sharedLink = client.sharing().createSharedLinkWithSettings(filePath, SharedLinkSettings.newBuilder().withRequestedVisibility(RequestedVisibility.PUBLIC).build());
        } catch (DbxException e) {
            e.printStackTrace();
        }

        return getDlUriToDropBoxImage(sharedLink.getUrl());
    }

    public void uploadApartmentImages(List<String> images, String userName, int apartmentId, List<Double> sizeInBytes) {
        for (int i = 0; i < images.size(); i++) {
            String[] data = images.get(i).split(",");
            String img;

            if (data.length == 2) {
                img = data[1];
            } else {
                img = images.get(1);
                new Thread(new UploadImage(img, userName, apartmentId, sizeInBytes.get(i))).start();
                break;
            }

            new Thread(new UploadImage(img, userName, apartmentId, sizeInBytes.get(i))).start();
        }
    }

    public void changeApartmentImages(List<String> images, String userName, int apartmentId, List<Double> sizeInBytes){
        Set<String> saveImagesLink = new HashSet<>();
        for (int i = 0; i < images.size(); i++) {
            String[] data = images.get(i).split(",");
            String img;

            if (data.length == 2) {
                img = data[1];
            } else {
                if(data[0].contains("https")){
                    saveImagesLink.add(data[0]);
                    continue;
                }

                img = images.get(1);
                new Thread(new UploadImage(img, userName, apartmentId, sizeInBytes.get(i))).start();
                break;
            }

            new Thread(new UploadImage(img, userName, apartmentId, sizeInBytes.get(i))).start();
        }

        for(ApartmentImage image : apartmentImageRepository.findByApartmentId(apartmentId)){
            if(!saveImagesLink.contains(image.getLinkPhoto())){
                try {
                    client.files().deleteV2(image.getPathPhoto());
                    apartmentImageRepository.delete(image);
                } catch (DbxException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class UploadImage implements Runnable {
        private String imgBase64;
        private String userEmail;
        private int apartmentId;
        private double sizeInBytes;

        UploadImage(String imgBase64, String userEmail, int apartmentId, double sizeInBytes) {
            this.imgBase64 = imgBase64;
            this.userEmail = userEmail;
            this.apartmentId = apartmentId;
            this.sizeInBytes = sizeInBytes;
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
                apartmentImageRepository.save(new ApartmentImage(filePath, getDlUriToDropBoxImage(url), new Apartment(apartmentId), sizeInBytes));
                firstImageUploaded = true;
                Time.sleep(500);
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
