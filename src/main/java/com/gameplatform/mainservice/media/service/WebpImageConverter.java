package com.gameplatform.mainservice.media.service;

import com.gameplatform.mainservice.exception.exceptions.MediaStorageException;
import com.luciad.imageio.webp.WebPWriteParam;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

@Component
public class WebpImageConverter {

    private static final float WEBP_COMPRESSION_QUALITY = 0.85f;
    private static final String WEBP_CONTENT_TYPE = "image/webp";
    private static final String WEBP_EXTENSION = "webp";

    public ConvertedImage convert(byte[] sourceBytes, String contentType) {
        if (WEBP_CONTENT_TYPE.equals(contentType)) {
            return new ConvertedImage(sourceBytes, contentType, WEBP_EXTENSION);
        }

        ImageWriter writer = ImageIO.getImageWritersByMIMEType(WEBP_CONTENT_TYPE).next();

        try (InputStream inputStream = new java.io.ByteArrayInputStream(sourceBytes);
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

            return new ConvertedImage(outputStream.toByteArray(), WEBP_CONTENT_TYPE, WEBP_EXTENSION);
        } catch (Exception e) {
            throw new MediaStorageException("Failed to convert image to WEBP", e);
        } finally {
            writer.dispose();
        }
    }

    public String resolveContentTypeByObjectKey(String objectKey) {
        if (objectKey == null) {
            return null;
        }

        String normalizedKey = objectKey.toLowerCase();
        if (normalizedKey.endsWith(".png")) {
            return "image/png";
        }
        if (normalizedKey.endsWith(".jpg") || normalizedKey.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (normalizedKey.endsWith(".webp")) {
            return WEBP_CONTENT_TYPE;
        }

        return null;
    }

    public record ConvertedImage(byte[] bytes, String contentType, String extension) {
        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof ConvertedImage that)) {
                return false;
            }
            return Arrays.equals(bytes, that.bytes)
                    && Objects.equals(contentType, that.contentType)
                    && Objects.equals(extension, that.extension);
        }

        @Override
        public int hashCode() {
            int result = Arrays.hashCode(bytes);
            result = 31 * result + Objects.hashCode(contentType);
            result = 31 * result + Objects.hashCode(extension);
            return result;
        }

        @Override
        public String toString() {
            return "ConvertedImage[bytes=" + Arrays.toString(bytes)
                    + ", contentType=" + contentType
                    + ", extension=" + extension + "]";
        }
    }
}
