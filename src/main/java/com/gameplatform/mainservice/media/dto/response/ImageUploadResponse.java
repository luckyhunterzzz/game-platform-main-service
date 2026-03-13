package com.gameplatform.mainservice.media.dto.response;

public record ImageUploadResponse(
        String bucket,
        String objectKey,
        String url
) {
}