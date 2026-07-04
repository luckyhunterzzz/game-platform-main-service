package com.gameplatform.mainservice.media.controller;

import com.gameplatform.mainservice.media.dto.response.ImageUploadResponse;
import com.gameplatform.mainservice.media.model.StoredImage;
import com.gameplatform.mainservice.media.service.MediaStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/media")
public class AdminMediaController {

    private final MediaStorageService mediaStorageService;

    @PostMapping(
            value = {"/images", "/images/publications"},
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ImageUploadResponse uploadPublicationImage(@RequestPart("file") MultipartFile file) {

        StoredImage storedImage = mediaStorageService.uploadPublicationImage(file);

        return new ImageUploadResponse(
                storedImage.bucket(),
                storedImage.objectKey(),
                storedImage.url()
        );
    }

    @PostMapping(
            value = "/images/heroes",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ImageUploadResponse uploadHeroImage(@RequestPart("file") MultipartFile file) {

        StoredImage storedImage = mediaStorageService.uploadHeroImage(file);

        return new ImageUploadResponse(
                storedImage.bucket(),
                storedImage.objectKey(),
                storedImage.url()
        );
    }

    @PostMapping(
            value = "/images/events",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ImageUploadResponse uploadEventImage(@RequestPart("file") MultipartFile file) {

        StoredImage storedImage = mediaStorageService.uploadEventImage(file);

        return new ImageUploadResponse(
                storedImage.bucket(),
                storedImage.objectKey(),
                storedImage.url()
        );
    }
}
