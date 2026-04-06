package com.gameplatform.mainservice.hero.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class HeroAdminCatalogRepositoryImpl implements HeroAdminCatalogRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Page<Long> findHeroIds(String search, Pageable pageable) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", pageable.getPageSize())
                .addValue("offset", pageable.getOffset());

        String whereClause = buildWhereClause(params, search);

        String selectSql = """
                SELECT h.id
                FROM heroes h
                """ + whereClause + """
                ORDER BY LOWER(COALESCE(h.name_json ->> 'ru', h.name_json ->> 'en', h.slug)) ASC, h.id ASC
                LIMIT :limit OFFSET :offset
                """;

        String countSql = """
                SELECT COUNT(*)
                FROM heroes h
                """ + whereClause;

        List<Long> ids = jdbcTemplate.query(
                selectSql,
                params,
                (rs, rowNum) -> rs.getLong("id")
        );

        Long total = jdbcTemplate.queryForObject(countSql, params, Long.class);
        return new PageImpl<>(ids, pageable, total == null ? 0L : total);
    }

    private String buildWhereClause(MapSqlParameterSource params, String search) {
        if (!StringUtils.hasText(search)) {
            return "";
        }

        params.addValue("search", "%" + search.trim().toLowerCase() + "%");
        return """
                WHERE LOWER(h.slug) LIKE :search
                   OR LOWER(COALESCE(h.name_json ->> 'ru', '')) LIKE :search
                   OR LOWER(COALESCE(h.name_json ->> 'en', '')) LIKE :search
                """;
    }
}
