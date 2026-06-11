package com.gameplatform.mainservice.hero.converter;

import com.gameplatform.mainservice.hero.domain.entity.HeroTag;
import com.gameplatform.mainservice.hero.domain.entity.HeroTagGroup;
import com.gameplatform.mainservice.hero.dto.response.HeroTagResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class HeroTagResponseConverter {

    public HeroTagResponse toResponse(HeroTag tag, HeroTagGroup group) {
        return new HeroTagResponse(
                tag.getId(),
                tag.getNameJson(),
                tag.getDescriptionJson(),
                tag.getGroupId(),
                group != null ? group.getNameJson() : null
        );
    }

    public List<HeroTagResponse> toResponseList(List<HeroTag> tags, List<HeroTagGroup> groups) {
        Map<Long, HeroTagGroup> groupsById = groups.stream()
                .collect(Collectors.toMap(HeroTagGroup::getId, Function.identity()));
        return tags.stream()
                .map(tag -> toResponse(tag, groupsById.get(tag.getGroupId())))
                .toList();
    }
}
