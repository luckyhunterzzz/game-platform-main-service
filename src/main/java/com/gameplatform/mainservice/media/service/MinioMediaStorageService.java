package com.gameplatform.mainservice.media.service;

import com.luciad.imageio.webp.WebPWriteParam;
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

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioMediaStorageService implements MediaStorageService {

    private static final float WEBP_COMPRESSION_QUALITY = 0.85f;

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

    private PreparedImage prepareImage(MultipartFile file) {
        String contentType = file.getContentType();
        if ("image/webp".equals(contentType)) {
            try {
                return new PreparedImage(file.getBytes(), contentType);
            } catch (Exception e) {
                throw new MediaStorageException("Failed to read image bytes", e);
            }
        }

        ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();

        try (InputStream inputStream = file.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             var imageOutputStream = ImageIO.createImageOutputStream(outputStream)) {

            BufferedImage sourceImage = ImageIO.read(inputStream);
            if (sourceImage == null) {
                throw new IllegalArgumentException("Failed to decode uploaded image");
            }

            WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionType(writeParam.getCompressionTypes()[0]);
            writeParam.setCompressionQuality(WEBP_COMPRESSION_QUALITY);

            writer.setOutput(imageOutputStream);
            writer.write(null, new IIOImage(sourceImage, null, null), writeParam);
            imageOutputStream.flush();

            return new PreparedImage(outputStream.toByteArray(), "image/webp");
        } catch (MediaStorageException e) {
            throw e;
        } catch (Exception e) {
            throw new MediaStorageException("Failed to convert image to WEBP", e);
        } finally {
            writer.dispose();
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
