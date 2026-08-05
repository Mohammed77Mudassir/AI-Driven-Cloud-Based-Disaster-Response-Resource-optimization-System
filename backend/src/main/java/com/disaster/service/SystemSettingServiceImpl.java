package com.disaster.service;

import com.disaster.dto.SystemSettingDTO;
import com.disaster.entity.SystemSetting;
import com.disaster.repository.SystemSettingRepository;
import javax.sql.DataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;
    private final DataSource dataSource;
    private final LocalDateTime startTime = LocalDateTime.now();

    public SystemSettingServiceImpl(SystemSettingRepository systemSettingRepository, DataSource dataSource) {
        this.systemSettingRepository = systemSettingRepository;
        this.dataSource = dataSource;
    }

    @Override
    public List<SystemSettingDTO> getAllSettings() {
        return systemSettingRepository.findAll().stream()
                .map(SystemSettingDTO::fromEntity).toList();
    }

    @Override
    public SystemSettingDTO getSetting(String key) {
        SystemSetting setting = systemSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new NoSuchElementException("Setting not found: " + key));
        return SystemSettingDTO.fromEntity(setting);
    }

    @Override
    @Transactional
    public SystemSettingDTO updateSetting(String key, String value) {
        SystemSetting setting = systemSettingRepository.findBySettingKey(key).orElse(null);
        if (setting == null) {
            setting = new SystemSetting();
            setting.setSettingKey(key);
        }
        setting.setSettingValue(value);
        return SystemSettingDTO.fromEntity(systemSettingRepository.save(setting));
    }

    @Override
    public String getSystemUptime() {
        Duration uptime = Duration.between(startTime, LocalDateTime.now());
        long days = uptime.toDays();
        long hours = uptime.toHours() % 24;
        long minutes = uptime.toMinutes() % 60;
        long seconds = uptime.getSeconds() % 60;
        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    }

    @Override
    public Map<String, Object> getSystemHealth() {
        boolean dbUp;
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            dbUp = rs.next();
        } catch (Exception e) {
            dbUp = false;
        }
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("status", dbUp ? "UP" : "DOWN");
        health.put("databaseStatus", dbUp ? "UP" : "DOWN");
        health.put("uptime", getSystemUptime());
        health.put("startTime", startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        health.put("activeSettings", systemSettingRepository.count());
        health.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return health;
    }
}
