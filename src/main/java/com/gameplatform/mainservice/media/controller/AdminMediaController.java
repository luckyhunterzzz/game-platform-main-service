package com.gameplatform.mainservice.media.controller;

import com.gameplatform.mainservice.media.dto.response.ImageUploadResponse;
import com.gameplatform.mainservice.media.service.MediaStorageService;
import com.gameplatform.mainservice.media.model.StoredImage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/media")
public class AdminMediaController {

    private final MediaStorageService mediaStorageService;

    @PostMapping(
            value = "/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ImageUploadResponse uploadImage(@RequestPart("file") MultipartFile file) {

        StoredImage storedImage = mediaStorageService.uploadImage(file);

        return new ImageUploadResponse(
                storedImage.bucket(),
                storedImage.objectKey(),
                storedImage.url()
        );
    }
}