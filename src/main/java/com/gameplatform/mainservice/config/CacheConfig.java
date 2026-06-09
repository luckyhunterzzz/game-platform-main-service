package com.gameplatform.mainservice.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    private final RedisCacheErrorHandler redisCacheErrorHandler;

    public CacheConfig(RedisCacheErrorHandler redisCacheErrorHandler) {
        this.redisCacheErrorHandler = redisCacheErrorHandler;
    }

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(ObjectMapper objectMapper) {
        ObjectMapper cacheObjectMapper = objectMapper.copy();
        cacheObjectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        return RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .prefixCacheNameWith("main-service:v2::")
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(cacheObjectMapper)
                ));
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
            RedisCacheConfiguration redisCacheConfiguration,
            @Value("${app.cache.ttl.hero-page:PT10M}") Duration heroPageTtl,
            @Value("${app.cache.ttl.hero-lookups:PT30M}") Duration heroLookupsTtl,
            @Value("${app.cache.ttl.hero-filters:PT1H}") Duration heroFiltersTtl,
            @Value("${app.cache.ttl.hero-details:PT30M}") Duration heroDetailsTtl,
            @Value("${app.cache.ttl.hero-opinions:PT30M}") Duration heroOpinionsTtl,
            @Value("${app.cache.ttl.hero-coach:PT30M}") Duration heroCoachTtl,
            @Value("${app.cache.ttl.outfitter:PT30M}") Duration outfitterTtl,
            @Value("${app.cache.ttl.publications:PT15M}") Duration publicationsTtl
    ) {
        return builder -> builder
                .withCacheConfiguration(CacheNames.PUBLIC_HEROES_PAGE, redisCacheConfiguration.entryTtl(heroPageTtl))
                .withCacheConfiguration(CacheNames.PUBLIC_HERO_NAMES, redisCacheConfiguration.entryTtl(heroLookupsTtl))
                .withCacheConfiguration(CacheNames.PUBLIC_HERO_BATCH, redisCacheConfiguration.entryTtl(heroLookupsTtl))
                .withCacheConfiguration(CacheNames.PUBLIC_HERO_SEARCH, redisCacheConfiguration.entryTtl(heroLookupsTtl))
                .withCacheConfiguration(CacheNames.PUBLIC_HERO_FILTERS, redisCacheConfiguration.entryTtl(heroFiltersTtl))
                .withCacheConfiguration(CacheNames.PUBLIC_HERO_DETAILS, redisCacheConfiguration.entryTtl(heroDetailsTtl))
                .withCacheConfiguration(CacheNames.PUBLIC_HERO_VARIANTS, redisCacheConfiguration.entryTtl(heroDetailsTtl))
                .withCacheConfiguration(CacheNames.PUBLIC_HERO_EXPERT_OPINIONS, redisCacheConfiguration.entryTtl(heroOpinionsTtl))
                .withCacheConfiguration(CacheNames.PUBLIC_HERO_COACH_PAGE, redisCacheConfiguration.entryTtl(heroCoachTtl))
                .withCacheConfiguration(CacheNames.PUBLIC_HERO_COACH_FORECAST, redisCacheConfiguration.entryTtl(heroCoachTtl))
                .withCacheConfiguration(CacheNames.PUBLIC_OUTFITTER_PAGE, redisCacheConfiguration.entryTtl(outfitterTtl))
                .withCacheConfiguration(CacheNames.PUBLIC_OUTFITTER_FORECAST, redisCacheConfiguration.entryTtl(outfitterTtl))
                .withCacheConfiguration(CacheNames.PUBLIC_PUBLICATIONS_FEED, redisCacheConfiguration.entryTtl(publicationsTtl));
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return redisCacheErrorHandler;
    }
}
