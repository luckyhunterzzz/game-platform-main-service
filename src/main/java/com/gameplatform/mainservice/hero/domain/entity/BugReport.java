package com.gameplatform.mainservice.hero.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "bug_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BugReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "hero_id", nullable = false)
    private Long heroId;
    @Column(name = "author_id")
    private UUID authorId;
    @Column(name = "author_name", nullable = false)
    private String authorName;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "is_open", nullable = false)
    private boolean isOpen;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    @Column(name = "closed_at")
    private OffsetDateTime closedAt;
    @Column(name = "closed_by")
    private UUID closedBy;
}
