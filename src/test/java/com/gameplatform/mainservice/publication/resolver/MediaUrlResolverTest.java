package com.gameplatform.mainservice.publication.resolver;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MediaUrlResolverTest {

    private final MediaUrlResolver mediaUrlResolver =
            new MediaUrlResolver("https://media.gameops-platform.dev", "https://ru-media.gameops-platform.dev");

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldUseDefaultMediaBaseWithoutRequestContext() {
        String resolvedUrl = mediaUrlResolver.resolveUrl("game-content", "heroes/hero.webp");

        assertEquals(
                "https://media.gameops-platform.dev/game-content/heroes/hero.webp",
                resolvedUrl
        );
    }

    @Test
    void shouldUseRuMediaBaseForRuApiHost() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-Host", "ru-api.gameops-platform.dev");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        String resolvedUrl = mediaUrlResolver.resolveUrl("game-content", "heroes/hero.webp");

        assertEquals(
                "https://ru-media.gameops-platform.dev/game-content/heroes/hero.webp",
                resolvedUrl
        );
    }
}
