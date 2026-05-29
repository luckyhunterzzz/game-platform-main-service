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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioMediaStorageService implements MediaStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final ImageUploadValidator imageUploadValidator;
    private final MediaUrlResolver mediaUrlResolver;
    private final WebpImageConverter webpImageConverter;

    @Override
    public StoredImage uploadPublicationImage(MultipartFile file) {
        return uploadImage(file, "publications");
    }

    @Override
    public StoredImage uploadHeroImage(MultipartFile file) {
        return uploadImage(file, "heroes");
    }

    @Override
    public StoredImage uploadHeroImage(String originalFilename, byte[] bytes, String contentType) {
        return uploadImage(bytes, contentType, "heroes");
    }

    private StoredImage uploadImage(MultipartFile file, String folder) {

        imageUploadValidator.validate(file);

        PreparedImage preparedImage = prepareImage(file);
        String extension = resolveExtension(preparedImage.contentType());

        String objectKey = generateObjectKey(folder, extension);

        try (InputStream inputStream = preparedImage.openStream()) {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectKey)
                            .stream(inputStream, preparedImage.size(), -1)
                            .contentType(preparedImage.contentType())
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

    private StoredImage uploadImage(byte[] bytes, String contentType, String folder) {

        imageUploadValidator.validate(bytes, contentType);

        PreparedImage preparedImage = prepareImage(bytes, contentType);
        String extension = resolveExtension(preparedImage.contentType());
        String objectKey = generateObjectKey(folder, extension);

        try (InputStream inputStream = preparedImage.openStream()) {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectKey)
                            .stream(inputStream, preparedImage.size(), -1)
                            .contentType(preparedImage.contentType())
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

    private PreparedImage prepareImage(MultipartFile file) {
        try {
            WebpImageConverter.ConvertedImage convertedImage = webpImageConverter.convert(
                    file.getBytes(),
                    file.getContentType()
            );
            return new PreparedImage(convertedImage.bytes(), convertedImage.contentType());
        } catch (Exception e) {
            throw new MediaStorageException("Failed to prepare image for upload", e);
        }
    }

    private PreparedImage prepareImage(byte[] bytes, String contentType) {
        try {
            WebpImageConverter.ConvertedImage convertedImage = webpImageConverter.convert(
                    bytes,
                    contentType
            );
            return new PreparedImage(convertedImage.bytes(), convertedImage.contentType());
        } catch (Exception e) {
            throw new MediaStorageException("Failed to prepare image for upload", e);
        }
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

    private record PreparedImage(byte[] bytes, String contentType) {
        private InputStream openStream() {
            return new ByteArrayInputStream(bytes);
        }

        private long size() {
            return bytes.length;
        }
    }
}
