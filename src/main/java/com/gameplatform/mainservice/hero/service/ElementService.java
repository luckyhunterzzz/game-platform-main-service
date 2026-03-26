package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.domain.entity.Element;
import com.gameplatform.mainservice.hero.dto.request.ElementCreateRequest;
import com.gameplatform.mainservice.hero.dto.request.ElementUpdateRequest;
import com.gameplatform.mainservice.hero.repository.ElementRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ElementService {

    private final ElementRepository elementRepository;

    public List<Element> getAll() {
        return elementRepository.findAll();
    }

    public Element getById(Long id) {
        return elementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Element not found: " + id));
    }

    public Element create(ElementCreateRequest request) {
        Element element = new Element();
        element.setNameJson(request.nameJson());

        return elementRepository.save(element);
    }

    public Element update(Long id, ElementUpdateRequest request) {
        Element element = getById(id);

        element.setNameJson(request.nameJson());

        return elementRepository.save(element);
    }

    public void delete(Long id) {
        if (!elementRepository.existsById(id)) {
            throw new EntityNotFoundException("Element not found: " + id);
        }
        elementRepository.deleteById(id);
    }
}