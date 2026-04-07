package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.domain.entity.Element;
import com.gameplatform.mainservice.hero.dto.request.ElementUpsertRequest;
import com.gameplatform.mainservice.hero.repository.ElementRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ElementService {

    private final ElementRepository elementRepository;

    public List<Element> getAll() {
        return elementRepository.findAll();
    }

    public Page<Element> getPage(int page, int size) {
        return elementRepository.findAll(PageRequest.of(page, size, Sort.by("id").ascending()));
    }

    public Element getById(Long id) {
        return elementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Element not found: " + id));
    }

    public Element create(ElementUpsertRequest request) {
        Element element = new Element();
        element.setNameJson(request.nameJson());

        return elementRepository.save(element);
    }

    public Element update(Long id, ElementUpsertRequest request) {
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
