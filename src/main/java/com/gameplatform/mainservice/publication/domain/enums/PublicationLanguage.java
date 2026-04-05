package com.gameplatform.mainservice.publication.domain.enums;

public enum PublicationLanguage {
    RU("ru"),
    EN("en");

    private final String jsonKey;

    PublicationLanguage(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    public String getJsonKey() {
        return jsonKey;
    }
}
