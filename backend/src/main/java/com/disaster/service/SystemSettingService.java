package com.disaster.service;

import com.disaster.dto.SystemSettingDTO;
import java.util.List;
import java.util.Map;

public interface SystemSettingService {
    List<SystemSettingDTO> getAllSettings();
    SystemSettingDTO getSetting(String key);
    SystemSettingDTO updateSetting(String key, String value);
    String getSystemUptime();
    Map<String, Object> getSystemHealth();
}
