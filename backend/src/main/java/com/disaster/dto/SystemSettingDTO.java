package com.disaster.dto;

import com.disaster.entity.SystemSetting;

public class SystemSettingDTO {
    private Long id;
    private String settingKey;
    private String settingValue;
    private String description;

    public SystemSettingDTO() {}

    public static SystemSettingDTO fromEntity(SystemSetting s) {
        SystemSettingDTO dto = new SystemSettingDTO();
        dto.setId(s.getId());
        dto.setSettingKey(s.getSettingKey());
        dto.setSettingValue(s.getSettingValue());
        dto.setDescription(s.getDescription());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }
    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
