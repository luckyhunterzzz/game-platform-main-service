package com.gameplatform.mainservice.hero.converter;

import com.gameplatform.mainservice.hero.dto.response.HeroCardResponse;
import com.gameplatform.mainservice.hero.repository.projection.HeroCardProjection;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HeroPublicResponseConverterTest {

    private final MediaUrlResolver mediaUrlResolver = mock(MediaUrlResolver.class);
    private final HeroPublicResponseConverter converter = new HeroPublicResponseConverter(mediaUrlResolver);

    @Test
    void shouldIncludeAvailableCostumeCountInHeroCard() {
        HeroCardProjection projection = mock(HeroCardProjection.class);
        when(projection.getId()).thenReturn(104L);
        when(projection.getBaseHeroId()).thenReturn(100L);
        when(projection.getIsCostume()).thenReturn(true);
        when(projection.getCostumeIndex()).thenReturn(4);
        when(projection.getAvailableCostumeCount()).thenReturn(5);

        HeroCardResponse response = converter.toCardResponse(projection);

        assertThat(response.id()).isEqualTo(104L);
        assertThat(response.baseHeroId()).isEqualTo(100L);
        assertThat(response.isCostume()).isTrue();
        assertThat(response.costumeIndex()).isEqualTo(4);
        assertThat(response.availableCostumeCount()).isEqualTo(5);
    }
}
