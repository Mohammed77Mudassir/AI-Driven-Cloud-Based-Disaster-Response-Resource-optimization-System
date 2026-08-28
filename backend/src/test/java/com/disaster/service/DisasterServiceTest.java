package com.disaster.service;

import com.disaster.config.WebSocketConfig;
import com.disaster.dto.DisasterRequest;
import com.disaster.dto.PublicDisasterReportRequest;
import com.disaster.entity.Disaster;
import com.disaster.entity.DisasterPriority;
import com.disaster.entity.Role;
import com.disaster.entity.StatusTimeline;
import com.disaster.entity.User;
import com.disaster.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Focused unit tests for the India coordinate validation applied to
 * user-created disasters and public citizen reports.
 */
class DisasterServiceTest {

    private static final Long USER_ID = 1L;

    private final DisasterRepository disasterRepository = mock(DisasterRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final StatusTimelineRepository statusTimelineRepository = mock(StatusTimelineRepository.class);
    private final DisasterCommentRepository disasterCommentRepository = mock(DisasterCommentRepository.class);
    private final DisasterAssignmentRepository disasterAssignmentRepository = mock(DisasterAssignmentRepository.class);
    private final DisasterAttachmentRepository disasterAttachmentRepository = mock(DisasterAttachmentRepository.class);
    private final AuditService auditService = mock(AuditService.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final SMSProvider smsProvider = mock(SMSProvider.class);
    private final EmailProvider emailProvider = mock(EmailProvider.class);
    private final WebSocketConfig webSocketConfig = mock(WebSocketConfig.class);

    private DisasterService service;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("tester", "tester@example.com", "password", Role.USER);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.findByRole(Role.ADMIN)).thenReturn(List.of());
        when(disasterRepository.save(any(Disaster.class))).thenAnswer(inv -> {
            Disaster d = inv.getArgument(0);
            d.setId(99L);
            return d;
        });
        when(statusTimelineRepository.save(any(StatusTimeline.class))).thenAnswer(inv -> inv.getArgument(0));

        service = new DisasterService(disasterRepository, userRepository, statusTimelineRepository,
                disasterCommentRepository, disasterAssignmentRepository, disasterAttachmentRepository,
                auditService, notificationService, smsProvider, emailProvider, webSocketConfig);
    }

    private DisasterRequest request(double latitude, double longitude) {
        DisasterRequest r = new DisasterRequest();
        r.setDisasterType("Flood");
        r.setDescription("Severe flooding near the river bank");
        r.setSeverity("High");
        r.setLocation("Test location");
        r.setLatitude(latitude);
        r.setLongitude(longitude);
        r.setPriority(DisasterPriority.HIGH);
        return r;
    }

    private PublicDisasterReportRequest publicRequest(double latitude, double longitude) {
        PublicDisasterReportRequest r = new PublicDisasterReportRequest();
        r.setReporterName("Public Citizen");
        r.setReporterMobile("+911234567890");
        r.setDisasterType("Earthquake");
        r.setSeverity("High");
        r.setDescription("Tremors felt in residential areas");
        r.setAddress("Test address");
        r.setLatitude(latitude);
        r.setLongitude(longitude);
        return r;
    }

    @Test
    void createDisasterAcceptsBengaluru() {
        assertEquals(12.9716, service.createDisaster(request(12.9716, 77.5946), USER_ID).getLatitude());
    }

    @Test
    void createDisasterAcceptsMumbai() {
        assertEquals(19.0760, service.createDisaster(request(19.0760, 72.8777), USER_ID).getLatitude());
    }

    @Test
    void createDisasterAcceptsChennai() {
        assertEquals(13.0827, service.createDisaster(request(13.0827, 80.2707), USER_ID).getLatitude());
    }

    @Test
    void createDisasterRejectsLatitudeOutsideIndia() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createDisaster(request(38.0, 77.5), USER_ID));
    }

    @Test
    void createDisasterRejectsLongitudeOutsideIndia() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createDisaster(request(20.0, 100.0), USER_ID));
    }

    @Test
    void createDisasterRejectsOrigin() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createDisaster(request(0.0, 0.0), USER_ID));
    }

    @Test
    void createPublicReportAcceptsValidCoordinates() {
        var response = service.createPublicReport(publicRequest(26.1445, 91.7362));
        assertNotNull(response.getReportId());
        assertEquals(26.1445, response.getLatitude());
        assertEquals(91.7362, response.getLongitude());
    }

    @Test
    void createPublicReportRejectsCoordinatesOutsideIndia() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createPublicReport(publicRequest(6.0, 80.0)));
    }

    @Test
    void createPublicReportRejectsOrigin() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createPublicReport(publicRequest(0.0, 0.0)));
    }

    @Test
    void createPublicReportRejectsNullCoordinates() {
        PublicDisasterReportRequest r = new PublicDisasterReportRequest();
        r.setReporterName("Public Citizen");
        r.setReporterMobile("+911234567890");
        r.setDisasterType("Earthquake");
        r.setSeverity("High");
        r.setDescription("Tremors felt in residential areas");
        r.setAddress("Test address");
        assertThrows(IllegalArgumentException.class, () -> service.createPublicReport(r));
    }
}
