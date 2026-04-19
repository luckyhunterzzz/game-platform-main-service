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

@Component
public class WebpImageConverter {

    private static final float WEBP_COMPRESSION_QUALITY = 0.85f;

    public ConvertedImage convert(byte[] sourceBytes, String contentType) {
        if ("image/webp".equals(contentType)) {
            return new ConvertedImage(sourceBytes, contentType, "webp");
        }

        ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();

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

            return new ConvertedImage(outputStream.toByteArray(), "image/webp", "webp");
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
            return "image/webp";
        }

        return null;
    }

    public record ConvertedImage(byte[] bytes, String contentType, String extension) {
    }
}
