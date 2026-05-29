package com.gameplatform.mainservice.hero.service.importer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameplatform.mainservice.hero.dto.external.ExternalHeroCatalogPayload;
import com.gameplatform.mainservice.hero.dto.external.ExternalLocalizedHeroRecord;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class HeroImportCatalogClient {

    private final ObjectMapper objectMapper;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(15))
            .readTimeout(Duration.ofSeconds(60))
            .callTimeout(Duration.ofSeconds(90))
            .build();

    public ExternalHeroCatalogPayload downloadCatalog(String sourceUrl) {
        byte[] payload = downloadPayload(sourceUrl);
        try {
            return objectMapper.readValue(payload, ExternalHeroCatalogPayload.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse external catalog", e);
        }
    }

    public Map<String, ExternalLocalizedHeroRecord> downloadLocalizedCatalog(String sourceUrl) {
        byte[] payload = downloadPayload(sourceUrl);
        try {
            JsonNode root = objectMapper.readTree(payload);
            if (root == null || !root.isObject()) {
                throw new IllegalStateException("Localized catalog root must be a JSON object");
            }

            JsonNode heroesNode = root.has("heroes") ? root.get("heroes") : root;
            if (heroesNode == null || !heroesNode.isObject()) {
                throw new IllegalStateException("Localized catalog heroes container must be a JSON object");
            }

            Map<String, ExternalLocalizedHeroRecord> records = new LinkedHashMap<>();
            Iterator<Map.Entry<String, JsonNode>> fields = heroesNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode value = field.getValue();
                if (!looksLikeLocalizedHeroRecord(value)) {
                    continue;
                }

                ExternalLocalizedHeroRecord record = objectMapper.treeToValue(value, ExternalLocalizedHeroRecord.class);
                records.put(field.getKey(), record);
            }

            return records;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse localized catalog", e);
        }
    }

    private boolean looksLikeLocalizedHeroRecord(JsonNode node) {
        return node != null
                && node.isObject()
                && node.hasNonNull("empuzzledName")
                && node.hasNonNull("name");
    }

    private byte[] downloadPayload(String sourceUrl) {
        Request request = new Request.Builder()
                .url(sourceUrl)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IllegalStateException("Catalog download failed with status " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IllegalStateException("Catalog response body is empty");
            }

            return body.bytes();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to download external catalog", e);
        }
    }
}
