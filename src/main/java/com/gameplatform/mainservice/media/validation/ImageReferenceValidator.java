package com.gameplatform.mainservice.media.validation;

import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import org.springframework.stereotype.Component;

@Component
public class ImageReferenceValidator {

    public void validate(String imageBucket, String imageObjectKey) {
        boolean bucketProvided = imageBucket != null && !imageBucket.isBlank();
        boolean objectProvided = imageObjectKey != null && !imageObjectKey.isBlank();

        if (bucketProvided != objectProvided) {
            throw new BusinessValidationException(
                    "imageBucket and imageObjectKey must be provided together or both be null"
            );
        }
    }
}