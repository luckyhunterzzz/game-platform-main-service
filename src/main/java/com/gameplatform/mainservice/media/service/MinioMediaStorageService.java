package com.gameplatform.mainservice.media.service;

import com.gameplatform.mainservice.exception.exceptions.MediaStorageException;
import com.gameplatform.mainservice.media.config.MinioProperties;
import com.gameplatform.mainservice.media.model.StoredImage;
import com.gameplatform.mainservice.media.validation.ImageUploadValidator;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioMediaStorageService implements MediaStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final ImageUploadValidator imageUploadValidator;
    private final MediaUrlResolver mediaUrlResolver;

    @Override
    public StoredImage uploadPublicationImage(MultipartFile file) {
        return uploadImage(file, "publications");
    }

    @Override
    public StoredImage uploadHeroImage(MultipartFile file) {
        return uploadImage(file, "heroes");
    }

    private StoredImage uploadImage(MultipartFile file, String folder) {

        imageUploadValidator.validate(file);

        String contentType = file.getContentType();
        String extension = resolveExtension(contentType);

        String objectKey = generateObjectKey(folder, extension);

        try (InputStream inputStream = file.getInputStream()) {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectKey)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );

        } catch (Exception e) {
            throw new MediaStorageException("Failed to upload image to MinIO", e);
        }

        String url = mediaUrlResolver.resolveUrl(
                minioProperties.getBucket(),
                objectKey
        );

        return new StoredImage(
                minioProperties.getBucket(),
                objectKey,
                url
        );
    }

    private String resolveExtension(String contentType) {
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/jpeg" -> "jpg";
            case "image/webp" -> "webp";
            default -> throw new IllegalArgumentException("Unsupported image type: " + contentType);
        };
    }

    private String generateObjectKey(String folder, String extension) {
        return folder + "/" + UUID.randomUUID() + "." + extension;
    }
}
