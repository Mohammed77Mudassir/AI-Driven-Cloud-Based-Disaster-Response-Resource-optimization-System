package com.disaster.service;

import com.disaster.dto.AnalyticsDTO;
import com.disaster.entity.Disaster;
import com.disaster.entity.DisasterStatus;
import com.disaster.entity.Resource;
import com.disaster.entity.StatusTimeline;
import com.disaster.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    private final DisasterRepository disasterRepository;
    private final HospitalRepository hospitalRepository;
    private final ShelterRepository shelterRepository;
    private final VolunteerRepository volunteerRepository;
    private final ResourceRepository resourceRepository;
    private final StatusTimelineRepository timelineRepository;

    public AnalyticsService(DisasterRepository disasterRepository, HospitalRepository hospitalRepository,
                            ShelterRepository shelterRepository, VolunteerRepository volunteerRepository,
                            ResourceRepository resourceRepository,
                            StatusTimelineRepository timelineRepository) {
        this.disasterRepository = disasterRepository;
        this.hospitalRepository = hospitalRepository;
        this.shelterRepository = shelterRepository;
        this.volunteerRepository = volunteerRepository;
        this.resourceRepository = resourceRepository;
        this.timelineRepository = timelineRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsDTO getAnalytics() {
        AnalyticsDTO dto = new AnalyticsDTO();
        List<Disaster> allDisasters = disasterRepository.findAll();

        dto.setTotalDisasters(allDisasters.size());
        dto.setTotalHospitals(hospitalRepository.count());
        dto.setTotalShelters(shelterRepository.count());
        dto.setTotalVolunteers(volunteerRepository.count());
        dto.setTotalResources(resourceRepository.count());

        Map<String, Long> byMonth = new TreeMap<>();
        Map<String, Long> byType = new HashMap<>();
        Map<String, Long> bySeverity = new HashMap<>();
        Map<String, Long> byStatus = new HashMap<>();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");

        for (Disaster d : allDisasters) {
            if (d.getDate() == null) continue;
            String month = d.getDate().format(fmt);
            byMonth.merge(month, 1L, Long::sum);
            byType.merge(d.getDisasterType(), 1L, Long::sum);
            bySeverity.merge(d.getSeverity(), 1L, Long::sum);
            byStatus.merge(d.getStatus().name(), 1L, Long::sum);
        }

        dto.setDisastersByMonth(byMonth);
        dto.setDisastersByType(byType);
        dto.setDisastersBySeverity(bySeverity);
        dto.setDisastersByStatus(byStatus);

        dto.setAverageResponseTimeHours(averageResponseTimeHours(allDisasters));
        dto.setAverageResolutionTimeHours(averageResolutionTimeHours(allDisasters));

        dto.setResourceUtilizationPercent(resourceUtilizationPercent());
        dto.setVolunteerActivityPercent(volunteerActivityPercent());

        double hospitalOcc = hospitalRepository.findAll().stream()
            .mapToInt(h -> {
                int cap = h.getAvailableBeds() + h.getIcuBeds();
                return cap > 0 ? Math.min(100, h.getIcuBeds() * 100 / cap) : 0;
            })
            .average().orElse(0);
        dto.setHospitalOccupancyPercent(Math.round(hospitalOcc));

        double shelterOcc = shelterRepository.findAll().stream()
            .mapToInt(s -> s.getCapacity() > 0 ? (s.getOccupancy() * 100 / s.getCapacity()) : 0)
            .average().orElse(0);
        dto.setShelterOccupancyPercent(Math.round(shelterOcc));

        return dto;
    }

    /**
     * Response time = time from report creation until the disaster first leaves
     * the PENDING state (i.e. when a responder actually took ownership), derived
     * from the authoritative status-timeline audit trail. Disasters still pending
     * are excluded from the average rather than being counted at "now".
     */
    private double averageResponseTimeHours(List<Disaster> disasters) {
        long total = 0;
        long count = 0;
        for (Disaster d : disasters) {
            if (d.getDate() == null) continue;
            LocalDateTime respondedAt = firstRespondedAt(d.getId());
            if (respondedAt == null) continue;
            total += Math.max(0, Duration.between(d.getDate(), respondedAt).toMinutes());
            count++;
        }
        return count > 0 ? Math.round(total / (double) count) / 60.0 : 0;
    }

    /**
     * Resolution time = time from report creation until the RESOLVED transition,
     * again taken from the status timeline.
     */
    private double averageResolutionTimeHours(List<Disaster> disasters) {
        long total = 0;
        long count = 0;
        for (Disaster d : disasters) {
            if (d.getDate() == null || d.getStatus() != DisasterStatus.RESOLVED) continue;
            LocalDateTime resolvedAt = resolvedAt(d.getId());
            if (resolvedAt == null) continue;
            total += Math.max(0, Duration.between(d.getDate(), resolvedAt).toMinutes());
            count++;
        }
        return count > 0 ? Math.round(total / (double) count) / 60.0 : 0;
    }

    private LocalDateTime firstRespondedAt(Long disasterId) {
        return timelineRepository.findByDisasterIdOrderByChangedAtAsc(disasterId).stream()
            .filter(t -> t.getToStatus() != null && !DisasterStatus.PENDING.name().equals(t.getToStatus()))
            .map(StatusTimeline::getChangedAt)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    private LocalDateTime resolvedAt(Long disasterId) {
        return timelineRepository.findByDisasterIdOrderByChangedAtAsc(disasterId).stream()
            .filter(t -> DisasterStatus.RESOLVED.name().equals(t.getToStatus()))
            .map(StatusTimeline::getChangedAt)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    /**
     * Resource utilization = share of total quantity that is currently deployed
     * or in maintenance (i.e. not available for dispatch).
     */
    private long resourceUtilizationPercent() {
        List<Resource> resources = resourceRepository.findAll();
        if (resources.isEmpty()) return 0;
        int total = resources.stream().mapToInt(Resource::getTotalQuantity).sum();
        if (total <= 0) return 0;
        int utilized = resources.stream()
                .mapToInt(r -> Math.max(0, r.getTotalQuantity() - r.getQuantity()))
                .sum();
        return Math.min(100, utilized * 100L / total);
    }

    private long volunteerActivityPercent() {
        long totalVol = volunteerRepository.count();
        if (totalVol <= 0) return 0;
        long unavailable = volunteerRepository.count() - volunteerRepository.findByAvailableTrue().size();
        return Math.min(100, unavailable * 100L / totalVol);
    }
}
