package com.gameplatform.mainservice.hero.service.importer;

import com.gameplatform.mainservice.media.model.StoredImage;
import com.gameplatform.mainservice.media.service.MediaStorageService;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class HeroImportImageService {

    private final MediaStorageService mediaStorageService;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(15))
            .readTimeout(Duration.ofSeconds(60))
            .callTimeout(Duration.ofSeconds(90))
            .build();

    public StoredImage tryDownloadAndStoreImage(String heroId, String imageUrl, String kind) {
        try {
            return downloadAndStoreImage(heroId, imageUrl, kind);
        } catch (Exception ignored) {
            return null;
        }
    }

    private StoredImage downloadAndStoreImage(String heroId, String imageUrl, String kind) {
        Request request = new Request.Builder()
                .url(imageUrl)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalStateException(kind + " image download failed with status " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IllegalStateException(kind + " image response body is empty");
            }

            byte[] bytes = body.bytes();
            String contentType = normalizeContentType(response.header("Content-Type"));
            return mediaStorageService.uploadHeroImage(heroId + "-" + kind + resolveFileExtension(contentType), bytes, contentType);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to download " + kind + " image", e);
        }
    }

    private String normalizeContentType(String rawContentType) {
        if (rawContentType == null) {
            return "image/webp";
        }

        int separatorIndex = rawContentType.indexOf(';');
        return separatorIndex >= 0
                ? rawContentType.substring(0, separatorIndex).trim()
                : rawContentType.trim();
    }

    private String resolveFileExtension(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".img";
        };
    }
}
