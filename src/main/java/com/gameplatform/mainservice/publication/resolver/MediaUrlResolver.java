package com.gameplatform.mainservice.publication.resolver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MediaUrlResolver {

    private final String publicBaseUrl;

    public MediaUrlResolver(@Value("${app.minio.public-base-url}") String publicBaseUrl) {
        this.publicBaseUrl = publicBaseUrl;
    }

    public String resolveUrl(String bucket, String objectKey) {
        if (bucket == null || objectKey == null) {
            return null;
        }

        String base = publicBaseUrl.endsWith("/")
                ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1)
                : publicBaseUrl;

        return String.format("%s/%s/%s", base, bucket, objectKey);
    }
}
