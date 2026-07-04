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
import java.util.Arrays;
import java.util.Objects;
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

    @Override
    public StoredImage uploadEventImage(MultipartFile file) {
        return uploadImage(file, "events");
    }

    private StoredImage uploadImage(MultipartFile file, String folder) {

        imageUploadValidator.validate(file);

        return uploadPreparedImage(prepareImage(file), folder);
    }

    private StoredImage uploadImage(byte[] bytes, String contentType, String folder) {

        imageUploadValidator.validate(bytes, contentType);

        return uploadPreparedImage(prepareImage(bytes, contentType), folder);
    }

    private StoredImage uploadPreparedImage(PreparedImage preparedImage, String folder) {
        String objectKey = generateObjectKey(folder, resolveExtension(preparedImage.contentType()));

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

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof PreparedImage that)) {
                return false;
            }
            return Arrays.equals(bytes, that.bytes)
                    && Objects.equals(contentType, that.contentType);
        }

        @Override
        public int hashCode() {
            int result = Arrays.hashCode(bytes);
            result = 31 * result + Objects.hashCode(contentType);
            return result;
        }

        @Override
        public String toString() {
            return "PreparedImage[bytes=" + Arrays.toString(bytes)
                    + ", contentType=" + contentType + "]";
        }
    }
}
