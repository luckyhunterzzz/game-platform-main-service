package com.gameplatform.mainservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class CacheConfigTest {

    private final RedisSerializationContext.SerializationPair<Object> serializationPair =
            new CacheConfig(mock(RedisCacheErrorHandler.class))
                    .redisCacheConfiguration(new ObjectMapper().findAndRegisterModules())
                    .getValueSerializationPair();

    @Test
    void shouldRoundTripFinalRecordWithTypeMetadata() {
        CacheValue source = new CacheValue("value");

        ByteBuffer serialized = serializationPair.write(source);
        Object restored = serializationPair.read(serialized.asReadOnlyBuffer());

        assertThat(asString(serialized)).contains("@class");
        assertThat(restored).isEqualTo(source);
    }

    @Test
    void shouldRoundTripListOfFinalRecords() {
        List<CacheValue> source = List.of(new CacheValue("first"), new CacheValue("second"));

        ByteBuffer serialized = serializationPair.write(source);
        Object restored = serializationPair.read(serialized.asReadOnlyBuffer());

        assertThat(restored).isEqualTo(source);
    }

    private record CacheValue(String value) {
    }

    private String asString(ByteBuffer source) {
        ByteBuffer copy = source.asReadOnlyBuffer();
        byte[] bytes = new byte[copy.remaining()];
        copy.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
