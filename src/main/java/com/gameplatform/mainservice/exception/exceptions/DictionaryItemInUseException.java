package com.gameplatform.mainservice.exception.exceptions;

import com.gameplatform.mainservice.hero.dto.response.HeroUsageReferenceResponse;

import java.util.List;

public class DictionaryItemInUseException extends RuntimeException {

    private final String code;
    private final List<HeroUsageReferenceResponse> heroes;

    public DictionaryItemInUseException(String message, List<HeroUsageReferenceResponse> heroes) {
        super(message);
        this.code = "ENTITY_IN_USE";
        this.heroes = heroes;
    }

    public String getCode() {
        return code;
    }

    public List<HeroUsageReferenceResponse> getHeroes() {
        return heroes;
    }
}
