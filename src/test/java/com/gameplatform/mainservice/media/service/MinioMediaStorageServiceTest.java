package com.gameplatform.mainservice.media.service;

import com.gameplatform.mainservice.media.config.MinioProperties;
import com.gameplatform.mainservice.media.model.StoredImage;
import com.gameplatform.mainservice.media.validation.ImageUploadValidator;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MinioMediaStorageServiceTest {

    private static final String BUCKET = "media";

    private final MinioClient minioClient = mock(MinioClient.class);
    private MinioMediaStorageService mediaStorageService;

    @BeforeEach
    void setUp() {
        MinioProperties minioProperties = new MinioProperties();
        minioProperties.setBucket(BUCKET);

        mediaStorageService = new MinioMediaStorageService(
                minioClient,
                minioProperties,
                new ImageUploadValidator(),
                new MediaUrlResolver("https://cdn.example.com")
        );
    }

    @Test
    void shouldConvertJpegUploadToWebp() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hero.jpg",
                "image/jpeg",
                createImageBytes("jpg", BufferedImage.TYPE_INT_RGB)
        );

        StoredImage storedImage = mediaStorageService.uploadHeroImage(file);

        ArgumentCaptor<PutObjectArgs> putObjectArgsCaptor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(putObjectArgsCaptor.capture());

        PutObjectArgs putObjectArgs = putObjectArgsCaptor.getValue();

        assertEquals(BUCKET, storedImage.bucket());
        assertTrue(storedImage.objectKey().startsWith("heroes/"));
        assertTrue(storedImage.objectKey().endsWith(".webp"));
        assertEquals("https://cdn.example.com/media/" + storedImage.objectKey(), storedImage.url());
        assertEquals("image/webp", putObjectArgs.contentType());
        assertEquals(BUCKET, putObjectArgs.bucket());
        assertTrue(putObjectArgs.object().startsWith("heroes/"));
        assertTrue(putObjectArgs.object().endsWith(".webp"));
    }

    @Test
    void shouldKeepWebpUploadWithoutConversion() throws Exception {
        byte[] webpBytes = "fake-webp".getBytes();
        ByteArrayOutputStream uploadedBytes = new ByteArrayOutputStream();
        doAnswer(invocation -> {
            PutObjectArgs args = invocation.getArgument(0);
            args.stream().transferTo(uploadedBytes);
            return null;
        }).when(minioClient).putObject(any(PutObjectArgs.class));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "hero.webp",
                "image/webp",
                webpBytes
        );

        mediaStorageService.uploadPublicationImage(file);

        ArgumentCaptor<PutObjectArgs> putObjectArgsCaptor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(putObjectArgsCaptor.capture());

        PutObjectArgs putObjectArgs = putObjectArgsCaptor.getValue();

        assertEquals("image/webp", putObjectArgs.contentType());
        assertTrue(putObjectArgs.object().startsWith("publications/"));
        assertTrue(putObjectArgs.object().endsWith(".webp"));
        assertEquals("fake-webp", uploadedBytes.toString(StandardCharsets.UTF_8));
    }

    private byte[] createImageBytes(String format, int imageType) throws Exception {
        BufferedImage image = new BufferedImage(8, 8, imageType);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        return outputStream.toByteArray();
    }
}
