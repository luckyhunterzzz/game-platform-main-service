package com.gameplatform.mainservice.hero.domain.enums;

public enum HeroLanguage {
    RU("ru"),
    EN("en");

    private final String jsonKey;

    HeroLanguage(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    public String getJsonKey() {
        return jsonKey;
    }
}