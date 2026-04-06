package com.gameplatform.mainservice.hero.repository;

import com.gameplatform.mainservice.hero.repository.projection.HeroCardRow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class HeroCatalogRepositoryImpl implements HeroCatalogRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Page<HeroCardRow> findReadyBaseHeroCards(
            String locale,
            String search,
            List<Long> elementIds,
            List<Long> rarityIds,
            List<Long> heroClassIds,
            List<Long> familyIds,
            List<Long> manaSpeedIds,
            List<Long> alphaTalentIds,
            Pageable pageable
    ) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("locale", locale)
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        String whereClause = buildWhereClause(
                params,
                search,
                elementIds,
                rarityIds,
                heroClassIds,
                familyIds,
                manaSpeedIds,
                alphaTalentIds
        );

        String selectSql = """
                SELECT
                    h.id AS id,
                    h.slug AS slug,
                    COALESCE(h.name_json ->> :locale, h.slug) AS name,
                    COALESCE(NULLIF(h.image_bucket_json ->> :locale, ''), NULLIF(h.image_bucket_json ->> 'ru', ''), NULLIF(h.image_bucket_json ->> 'en', '')) AS imageBucket,
                    COALESCE(NULLIF(h.image_object_key_json ->> :locale, ''), NULLIF(h.image_object_key_json ->> 'ru', ''), NULLIF(h.image_object_key_json ->> 'en', '')) AS imageObjectKey,
                    e.name_json ->> :locale AS elementName,
                    r.name_json ->> :locale AS rarityName,
                    r.stars AS rarityStars,
                    hc.name_json ->> :locale AS heroClassName,
                    ms.name_json ->> :locale AS manaSpeedName,
                    f.name_json ->> :locale AS familyName,
                    at.name_json ->> :locale AS alphaTalentName,
                    h.base_attack AS baseAttack,
                    h.base_armor AS baseArmor,
                    h.base_hp AS baseHp
                FROM heroes h
                JOIN elements e ON e.id = h.element_id
                JOIN rarities r ON r.id = h.rarity_id
                JOIN hero_classes hc ON hc.id = h.hero_class_id
                JOIN mana_speeds ms ON ms.id = h.mana_speed_id
                LEFT JOIN families f ON f.id = h.family_id
                LEFT JOIN alpha_talents at ON at.id = h.alpha_talent_id
                """ + whereClause + """
                ORDER BY LOWER(COALESCE(h.name_json ->> :locale, h.slug)) ASC, h.id ASC
                LIMIT :limit OFFSET :offset
                """;

        String countSql = """
                SELECT COUNT(*)
                FROM heroes h
                """ + whereClause;

        List<HeroCardRow> items = jdbcTemplate.query(
                selectSql,
                params,
                (rs, rowNum) -> new HeroCardRow(
                        rs.getLong("id"),
                        rs.getString("slug"),
                        rs.getString("name"),
                        rs.getString("imageBucket"),
                        rs.getString("imageObjectKey"),
                        rs.getString("elementName"),
                        rs.getString("rarityName"),
                        (Integer) rs.getObject("rarityStars"),
                        rs.getString("heroClassName"),
                        rs.getString("manaSpeedName"),
                        rs.getString("familyName"),
                        rs.getString("alphaTalentName"),
                        (Integer) rs.getObject("baseAttack"),
                        (Integer) rs.getObject("baseArmor"),
                        (Integer) rs.getObject("baseHp")
                )
        );

        Long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        return new PageImpl<>(items, pageable, total == null ? 0L : total);
    }

    private String buildWhereClause(
            MapSqlParameterSource params,
            String search,
            List<Long> elementIds,
            List<Long> rarityIds,
            List<Long> heroClassIds,
            List<Long> familyIds,
            List<Long> manaSpeedIds,
            List<Long> alphaTalentIds
    ) {
        List<String> conditions = new ArrayList<>();
        conditions.add("h.status = 'READY'");
        conditions.add("h.is_costume = false");

        if (StringUtils.hasText(search)) {
            conditions.add("LOWER(COALESCE(h.name_json ->> :locale, h.slug)) LIKE :search");
            params.addValue("search", "%" + search.trim().toLowerCase() + "%");
        }

        addInFilter(conditions, params, "h.element_id", "elementIds", elementIds);
        addInFilter(conditions, params, "h.rarity_id", "rarityIds", rarityIds);
        addInFilter(conditions, params, "h.hero_class_id", "heroClassIds", heroClassIds);
        addInFilter(conditions, params, "h.family_id", "familyIds", familyIds);
        addInFilter(conditions, params, "h.mana_speed_id", "manaSpeedIds", manaSpeedIds);
        addInFilter(conditions, params, "h.alpha_talent_id", "alphaTalentIds", alphaTalentIds);

        return "\nWHERE " + String.join("\n  AND ", conditions) + "\n";
    }

    private void addInFilter(
            List<String> conditions,
            MapSqlParameterSource params,
            String columnName,
            String paramName,
            List<Long> ids
    ) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        conditions.add(columnName + " IN (:" + paramName + ")");
        params.addValue(paramName, ids);
    }
}
