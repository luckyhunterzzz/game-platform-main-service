package com.gameplatform.mainservice.hero.mapper;

import com.gameplatform.mainservice.hero.domain.entity.Element;
import com.gameplatform.mainservice.hero.dto.response.ElementResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ElementResponseConverter {

    public ElementResponse toResponse(Element entity) {
        return new ElementResponse(
                entity.getId(),
                entity.getNameJson()
        );
    }

    public List<ElementResponse> toResponseList(List<Element> entities) {
        return entities.stream()
                .map(this::toResponse)
                .toList();
    }
}