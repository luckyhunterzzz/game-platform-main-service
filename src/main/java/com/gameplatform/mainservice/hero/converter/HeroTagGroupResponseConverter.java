package com.gameplatform.mainservice.hero.converter;

import com.gameplatform.mainservice.hero.domain.entity.HeroTag;
import com.gameplatform.mainservice.hero.domain.entity.HeroTagGroup;
import com.gameplatform.mainservice.hero.dto.response.HeroTagGroupResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroTagReferenceResponse;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class HeroTagGroupResponseConverter {

    public HeroTagGroupResponse toResponse(HeroTagGroup group, List<HeroTag> tags) {
        List<HeroTagReferenceResponse> tagResponses = tags.stream()
                .filter(tag -> group.getId().equals(tag.getGroupId()))
                .sorted(Comparator.comparing(tag -> safeLocalized(tag.getNameJson())))
                .map(tag -> new HeroTagReferenceResponse(tag.getId(), tag.getNameJson()))
                .toList();

        return new HeroTagGroupResponse(
                group.getId(),
                group.getNameJson(),
                group.getDescriptionJson(),
                tagResponses
        );
    }

    public List<HeroTagGroupResponse> toResponseList(List<HeroTagGroup> groups, List<HeroTag> tags) {
        return groups.stream()
                .map(group -> toResponse(group, tags))
                .toList();
    }

    private String safeLocalized(com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson value) {
        if (value == null) {
            return "";
        }
        if (value.ru() != null && !value.ru().isBlank()) {
            return value.ru().trim().toLowerCase();
        }
        return value.en() == null ? "" : value.en().trim().toLowerCase();
    }
}
