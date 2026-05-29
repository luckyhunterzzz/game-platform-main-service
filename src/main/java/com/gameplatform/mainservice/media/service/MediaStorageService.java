package com.gameplatform.mainservice.media.service;

import com.gameplatform.mainservice.media.model.StoredImage;
import org.springframework.web.multipart.MultipartFile;

public interface MediaStorageService {
    StoredImage uploadPublicationImage(MultipartFile file);

    StoredImage uploadHeroImage(MultipartFile file);

    StoredImage uploadHeroImage(String originalFilename, byte[] bytes, String contentType);
}
