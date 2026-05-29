package com.gameplatform.mainservice.settings.repository;

import com.gameplatform.mainservice.settings.domain.entity.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSettingRepository extends JpaRepository<AppSetting, String> {
}
