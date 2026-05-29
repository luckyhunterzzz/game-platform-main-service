package com.gameplatform.mainservice.settings.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "app_settings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppSetting {

    @Id
    @Column(name = "setting_key", nullable = false, length = 150)
    private String settingKey;

    @Column(name = "setting_value", nullable = false, length = 255)
    private String settingValue;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_by_email")
    private String updatedByEmail;
}
