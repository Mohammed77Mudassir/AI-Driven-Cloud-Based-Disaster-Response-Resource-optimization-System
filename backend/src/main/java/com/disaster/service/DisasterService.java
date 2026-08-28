package com.disaster.service;

import com.disaster.dto.*;
import com.disaster.entity.*;
import com.disaster.exception.ResourceNotFoundException;
import com.disaster.repository.*;
import com.disaster.config.WebSocketConfig;
import com.disaster.geo.GeoUtils;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DisasterService {
    private final DisasterRepository disasterRepository;
    private final UserRepository userRepository;
    private final StatusTimelineRepository statusTimelineRepository;
    private final DisasterCommentRepository disasterCommentRepository;
    private final DisasterAssignmentRepository disasterAssignmentRepository;
    private final DisasterAttachmentRepository disasterAttachmentRepository;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final SMSProvider smsProvider;
    private final EmailProvider emailProvider;
    private final WebSocketConfig webSocketConfig;

    public DisasterService(DisasterRepository disasterRepository, UserRepository userRepository,
                           StatusTimelineRepository statusTimelineRepository,
                           DisasterCommentRepository disasterCommentRepository,
                           DisasterAssignmentRepository disasterAssignmentRepository,
                           DisasterAttachmentRepository disasterAttachmentRepository,
                           AuditService auditService,
                           NotificationService notificationService, SMSProvider smsProvider,
                           EmailProvider emailProvider, WebSocketConfig webSocketConfig) {
        this.disasterRepository = disasterRepository;
        this.userRepository = userRepository;
        this.statusTimelineRepository = statusTimelineRepository;
        this.disasterCommentRepository = disasterCommentRepository;
        this.disasterAssignmentRepository = disasterAssignmentRepository;
        this.disasterAttachmentRepository = disasterAttachmentRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.smsProvider = smsProvider;
        this.emailProvider = emailProvider;
        this.webSocketConfig = webSocketConfig;
    }

    // ------------------------------------------------------------------
    // Status workflow
    // ------------------------------------------------------------------

    private static final Map<DisasterStatus, Set<DisasterStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(DisasterStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(DisasterStatus.PENDING, EnumSet.of(DisasterStatus.VERIFIED, DisasterStatus.ASSIGNED));
        ALLOWED_TRANSITIONS.put(DisasterStatus.VERIFIED, EnumSet.of(DisasterStatus.ASSIGNED, DisasterStatus.IN_PROGRESS));
        ALLOWED_TRANSITIONS.put(DisasterStatus.ASSIGNED, EnumSet.of(DisasterStatus.RESOURCES_DISPATCHED, DisasterStatus.IN_PROGRESS));
        ALLOWED_TRANSITIONS.put(DisasterStatus.RESOURCES_DISPATCHED, EnumSet.of(DisasterStatus.IN_PROGRESS));
        ALLOWED_TRANSITIONS.put(DisasterStatus.IN_PROGRESS, EnumSet.of(DisasterStatus.RESOLVED));
        ALLOWED_TRANSITIONS.put(DisasterStatus.RESOLVED, EnumSet.noneOf(DisasterStatus.class));
    }

    private void assertValidTransition(DisasterStatus from, DisasterStatus to) {
        if (from == to) return;
        Set<DisasterStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new IllegalArgumentException(
                    "Invalid status transition: " + from + " -> " + to +
                            ". Allowed: " + (allowed.isEmpty() ? "none (terminal state)" : allowed));
        }
    }

    /**
     * Broadcasts a disaster event over the unauthenticated WebSocket feed
     * with reporter PII stripped (mobile / email).
     */
    private void broadcastDisaster(Disaster disaster) {
        webSocketConfig.broadcastUpdate("DISASTER", DisasterResponse.fromEntity(disaster).redactReporterInfo());
    }

    private void assertInsideIndia(double latitude, double longitude) {
        if (!GeoUtils.isInsideIndia(latitude, longitude)) {
            throw new IllegalArgumentException(
                    "Coordinates (" + latitude + ", " + longitude + ") are outside the supported India region");
        }
    }

    // ------------------------------------------------------------------
    // Authenticated disaster reporting
    // ------------------------------------------------------------------

    @Transactional
    public DisasterResponse createDisaster(DisasterRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        assertInsideIndia(request.getLatitude(), request.getLongitude());
        Disaster disaster = new Disaster();
        disaster.setDisasterType(request.getDisasterType());
        disaster.setDescription(request.getDescription());
        disaster.setSeverity(request.getSeverity());
        disaster.setLocation(request.getLocation());
        disaster.setLatitude(request.getLatitude());
        disaster.setLongitude(request.getLongitude());
        disaster.setDate(LocalDateTime.now());
        disaster.setStatus(DisasterStatus.PENDING);
        disaster.setPriority(request.getPriority() != null ? request.getPriority() : DisasterPriority.MEDIUM);
        disaster.setUser(user);
        Disaster saved = disasterRepository.save(disaster);
        persistAttachments(saved, request.getAttachments());

        addTimeline(saved, null, DisasterStatus.PENDING.name(), user, "Disaster reported");
        auditService.log("CREATE", "Disaster", saved.getId(), user.getUsername(),
                "Reported " + request.getDisasterType() + " at " + request.getLocation());

        String msg = "New disaster reported: " + request.getDisasterType() + " at " + request.getLocation();
        notificationService.createNotification(userId, "Disaster Reported", msg, "INFO");
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            smsProvider.sendSMS(user.getPhone(), msg);
        }
        emailProvider.sendEmail(user.getEmail(), "Disaster Reported - " + request.getDisasterType(), msg);

        broadcastDisaster(saved);
        return DisasterResponse.fromEntity(saved);
    }

    /**
     * Editable fields an authorized user may update on an existing disaster.
     * Core fields such as report id, reporter identity and source are immutable.
     */
    @Transactional
    public DisasterResponse updateDisaster(Long disasterId, DisasterRequest request, String username) {
        Disaster disaster = disasterRepository.findById(disasterId)
                .orElseThrow(() -> new ResourceNotFoundException("Disaster not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        disaster.setDisasterType(request.getDisasterType());
        disaster.setDescription(request.getDescription());
        disaster.setSeverity(request.getSeverity());
        disaster.setLocation(request.getLocation());
        disaster.setAddress(request.getAddress() != null ? request.getAddress() : request.getLocation());
        disaster.setLatitude(request.getLatitude());
        disaster.setLongitude(request.getLongitude());
        if (request.getPriority() != null) {
            disaster.setPriority(request.getPriority());
        }

        Disaster saved = disasterRepository.save(disaster);
        addTimeline(saved, saved.getStatus().name(), saved.getStatus().name(), user,
                "Disaster details updated by " + username);

        auditService.log("UPDATE", "Disaster", saved.getId(), username,
                "Updated " + request.getDisasterType() + " at " + request.getLocation());

        broadcastDisaster(saved);
        return DisasterResponse.fromEntity(saved);
    }

    @Transactional
    public DisasterResponse updatePriority(Long disasterId, DisasterPriority priority, String username) {
        Disaster disaster = disasterRepository.findById(disasterId)
                .orElseThrow(() -> new ResourceNotFoundException("Disaster not found"));
        DisasterPriority oldPriority = disaster.getPriority();
        disaster.setPriority(priority);
        Disaster saved = disasterRepository.save(disaster);

        addTimeline(saved, saved.getStatus().name(), saved.getStatus().name(),
                userRepository.findByUsername(username).orElse(null),
                "Priority changed from " + oldPriority + " to " + priority);

        auditService.log("PRIORITY_CHANGE", "Disaster", saved.getId(), username,
                oldPriority + " -> " + priority);

        broadcastDisaster(saved);
        return DisasterResponse.fromEntity(saved);
    }

    /**
     * Exposes the allowed status workflow so clients can render an accurate
     * status-transition UI without duplicating the policy.
     */
    @Transactional(readOnly = true)
    public Map<String, List<String>> getStatusFlow() {
        Map<String, List<String>> flow = new LinkedHashMap<>();
        for (DisasterStatus status : DisasterStatus.values()) {
            flow.put(status.name(), ALLOWED_TRANSITIONS.getOrDefault(status, Set.of())
                    .stream().map(Enum::name).sorted().toList());
        }
        return flow;
    }

    @Transactional(readOnly = true)
    public List<DisasterResponse> getUserDisasters(Long userId) {
        return disasterRepository.findByUserIdOrderByDateDesc(userId)
                .stream().map(DisasterResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public List<DisasterResponse> getAllDisasters() {
        return disasterRepository.findAllByOrderByDateDesc()
                .stream().map(DisasterResponse::fromEntity).toList();
    }

    // ------------------------------------------------------------------
    // Search / filter / sort / pagination
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PageResponse<DisasterResponse> queryDisasters(Long userId, String search, String type,
                                                         String severity, String status, String priority,
                                                         String source, int page, int size,
                                                         String sortBy, String sortDir) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);
        Specification<Disaster> spec = buildSpecification(userId, search, type, severity, status, priority, source);
        Page<Disaster> result = disasterRepository.findAll(spec, pageable);
        return PageResponse.from(result, DisasterResponse::fromEntity,
                normalizeSortBy(sortBy), normalizeSortDir(sortDir));
    }

    private Specification<Disaster> buildSpecification(Long userId, String search, String type,
                                                       String severity, String status, String priority,
                                                       String source) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (search != null && !search.isBlank()) {
                String like = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("disasterType")), like),
                        cb.like(cb.lower(root.get("location")), like),
                        cb.like(cb.lower(root.get("description")), like),
                        cb.like(cb.lower(root.get("reporterName")), like),
                        cb.like(cb.lower(root.get("reportId")), like)));
            }
            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("disasterType")), type.trim().toLowerCase()));
            }
            if (severity != null && !severity.isBlank()) {
                predicates.add(cb.equal(root.get("severity"), severity));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), DisasterStatus.valueOf(status)));
            }
            if (priority != null && !priority.isBlank()) {
                predicates.add(cb.equal(root.get("priority"), DisasterPriority.valueOf(priority)));
            }
            if (source != null && !source.isBlank()) {
                predicates.add(cb.equal(root.get("source"), source));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static final List<String> SORTABLE_FIELDS = List.of(
            "date", "disasterType", "severity", "status", "priority", "location", "createdAt");

    private String normalizeSortBy(String sortBy) {
        if (sortBy == null || sortBy.isBlank() || !SORTABLE_FIELDS.contains(sortBy)) {
            return "date";
        }
        return sortBy;
    }

    private String normalizeSortDir(String sortDir) {
        return (sortDir != null && "asc".equalsIgnoreCase(sortDir)) ? "asc" : "desc";
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(safePage, safeSize, Sort.by(direction, normalizeSortBy(sortBy)));
    }

    // ------------------------------------------------------------------
    // Detail, status, delete
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public DisasterResponse getDisaster(Long disasterId) {
        Disaster disaster = disasterRepository.findById(disasterId)
                .orElseThrow(() -> new ResourceNotFoundException("Disaster not found"));
        List<DisasterAttachment> attachments =
                disasterAttachmentRepository.findByDisasterIdOrderByCreatedAtAsc(disasterId);
        return DisasterResponse.fromEntity(disaster, attachments);
    }

    @Transactional(readOnly = true)
    public DisasterDetailDTO getDisasterDetail(Long disasterId) {
        Disaster disaster = disasterRepository.findById(disasterId)
                .orElseThrow(() -> new ResourceNotFoundException("Disaster not found"));

        List<AttachmentDTO> attachments = disasterAttachmentRepository
                .findByDisasterIdOrderByCreatedAtAsc(disasterId).stream()
                .map(a -> AttachmentDTO.fromEntity(a, true))
                .toList();
        List<StatusTimelineDTO> timeline = statusTimelineRepository
                .findByDisasterIdOrderByChangedAtAsc(disasterId).stream()
                .map(StatusTimelineDTO::fromEntity)
                .toList();
        List<DisasterCommentDTO> comments = disasterCommentRepository
                .findByDisasterIdOrderByCreatedAtDesc(disasterId).stream()
                .map(DisasterCommentDTO::fromEntity)
                .toList();
        List<DisasterAssignmentDTO> assignments = disasterAssignmentRepository
                .findByDisasterIdOrderByAssignedAtDesc(disasterId).stream()
                .map(DisasterAssignmentDTO::fromEntity)
                .toList();

        DisasterDetailDTO dto = new DisasterDetailDTO();
        dto.setId(disaster.getId());
        dto.setDisasterType(disaster.getDisasterType());
        dto.setDescription(disaster.getDescription());
        dto.setSeverity(disaster.getSeverity());
        dto.setStatus(disaster.getStatus() != null ? disaster.getStatus().name() : null);
        dto.setPriority(disaster.getPriority() != null ? disaster.getPriority().name() : null);
        dto.setLocation(disaster.getLocation());
        dto.setLatitude(disaster.getLatitude());
        dto.setLongitude(disaster.getLongitude());
        dto.setDate(disaster.getDate());
        dto.setReportedBy(disaster.getUser() != null ? disaster.getUser().getUsername() : disaster.getReporterName());
        dto.setReportId(disaster.getReportId());
        dto.setReporterName(disaster.getReporterName());
        dto.setReporterMobile(disaster.getReporterMobile());
        dto.setReporterEmail(disaster.getReporterEmail());
        dto.setAddress(disaster.getAddress());
        dto.setSource(disaster.getSource());
        dto.setCreatedAt(disaster.getCreatedAt());
        dto.setUpdatedAt(disaster.getUpdatedAt());
        dto.setAttachments(attachments);
        dto.setTimeline(timeline);
        dto.setComments(comments);
        dto.setAssignments(assignments);
        return dto;
    }

    @Transactional
    public DisasterResponse updateStatus(Long disasterId, StatusUpdateRequest request, String username) {
        Disaster disaster = disasterRepository.findById(disasterId)
                .orElseThrow(() -> new ResourceNotFoundException("Disaster not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        DisasterStatus oldStatus = disaster.getStatus();
        DisasterStatus newStatus = request.getStatus();
        assertValidTransition(oldStatus, newStatus);
        disaster.setStatus(newStatus);
        Disaster saved = disasterRepository.save(disaster);

        addTimeline(saved, oldStatus.name(), newStatus.name(), user,
                request.getComment() != null && !request.getComment().isBlank()
                        ? request.getComment() : "Status updated to " + newStatus);

        auditService.log("STATUS_CHANGE", "Disaster", saved.getId(), username,
                oldStatus + " -> " + newStatus);

        String msg = "Disaster #" + disasterId + " status changed from " + oldStatus + " to " + newStatus;
        if (disaster.getUser() != null) {
            notificationService.createNotification(disaster.getUser().getId(), "Status Updated", msg, "INFO");
        }
        if (disaster.getReporterMobile() != null && !disaster.getReporterMobile().isBlank()) {
            smsProvider.sendSMS(disaster.getReporterMobile(), msg);
        } else if (disaster.getUser() != null && disaster.getUser().getPhone() != null
                && !disaster.getUser().getPhone().isBlank()) {
            smsProvider.sendSMS(disaster.getUser().getPhone(), msg);
        }
        if (disaster.getReporterEmail() != null && !disaster.getReporterEmail().isBlank()) {
            emailProvider.sendEmail(disaster.getReporterEmail(), "Disaster Status Update - " + disaster.getReportId(), msg);
        } else if (disaster.getUser() != null) {
            emailProvider.sendEmail(disaster.getUser().getEmail(), "Disaster Status Update", msg);
        }

        broadcastDisaster(saved);
        return DisasterResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteDisaster(Long disasterId) {
        Disaster disaster = disasterRepository.findById(disasterId)
                .orElseThrow(() -> new ResourceNotFoundException("Disaster not found"));
        disaster.setDeleted(true);
        disasterRepository.save(disaster);
        auditService.log("DELETE", "Disaster", disasterId, "system",
                "Soft-deleted " + disaster.getDisasterType() + " at " + disaster.getLocation());
    }

    // ------------------------------------------------------------------
    // Timeline
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<StatusTimelineDTO> getTimeline(Long disasterId) {
        return statusTimelineRepository.findByDisasterIdOrderByChangedAtAsc(disasterId)
                .stream().map(StatusTimelineDTO::fromEntity).toList();
    }

    private void addTimeline(Disaster disaster, String fromStatus, String toStatus, User changedBy, String comment) {
        StatusTimeline timeline = new StatusTimeline();
        timeline.setDisaster(disaster);
        timeline.setFromStatus(fromStatus);
        timeline.setToStatus(toStatus);
        timeline.setChangedAt(LocalDateTime.now());
        timeline.setChangedBy(changedBy);
        timeline.setComment(comment);
        statusTimelineRepository.save(timeline);
    }

    // ------------------------------------------------------------------
    // Comments
    // ------------------------------------------------------------------

    @Transactional
    public DisasterCommentDTO addComment(Long disasterId, CommentRequest request, Long userId) {
        Disaster disaster = disasterRepository.findById(disasterId)
                .orElseThrow(() -> new ResourceNotFoundException("Disaster not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        DisasterComment comment = disasterCommentRepository.save(
                new DisasterComment(disaster, user, request.getText().trim()));
        auditService.log("COMMENT", "Disaster", disasterId, user.getUsername(), "Added comment to disaster");
        return DisasterCommentDTO.fromEntity(comment);
    }

    @Transactional(readOnly = true)
    public List<DisasterCommentDTO> getComments(Long disasterId) {
        return disasterCommentRepository.findByDisasterIdOrderByCreatedAtDesc(disasterId)
                .stream().map(DisasterCommentDTO::fromEntity).toList();
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId, Role role) {
        DisasterComment comment = disasterCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        if (role != Role.ADMIN && (comment.getAuthor() == null || !comment.getAuthor().getId().equals(userId))) {
            throw new AccessDeniedException("You can only delete your own comments");
        }
        disasterCommentRepository.delete(comment);
    }

    // ------------------------------------------------------------------
    // Assignment history
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<DisasterAssignmentDTO> getAssignments(Long disasterId) {
        return disasterAssignmentRepository.findByDisasterIdOrderByAssignedAtDesc(disasterId)
                .stream().map(DisasterAssignmentDTO::fromEntity).toList();
    }

    @Transactional
    public void recordAssignment(Long disasterId, RescueTeam team, String assignedBy, String action) {
        Disaster disaster = disasterRepository.findById(disasterId)
                .orElseThrow(() -> new ResourceNotFoundException("Disaster not found"));
        DisasterAssignment assignment = new DisasterAssignment(disaster, team, assignedBy, action);
        if ("RELEASED".equals(action)) {
            assignment.setReleasedAt(LocalDateTime.now());
        }
        disasterAssignmentRepository.save(assignment);
        auditService.log("ASSIGNMENT_" + action, "Disaster", disasterId, assignedBy,
                (team != null ? team.getTeamName() : "team") + " " + action.toLowerCase() + " to disaster");
    }

    // ------------------------------------------------------------------
    // Public citizen reporting (no login required)
    // ------------------------------------------------------------------

    private static final String ALPHANUM = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    /** Allowed attachment MIME types (defense in depth on top of bean validation). */
    private static final java.util.Set<String> ALLOWED_MIME = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp",
            "application/pdf",
            "video/mp4", "video/webm", "video/quicktime", "video/x-msvideo");

    private static final long MAX_ATTACHMENT_BYTES = 5_000_000L;

    @Transactional
    public PublicReportDTO createPublicReport(PublicDisasterReportRequest request) {
        assertInsideIndia(request.getLatitude() != null ? request.getLatitude() : 0.0,
                request.getLongitude() != null ? request.getLongitude() : 0.0);
        Disaster disaster = new Disaster();
        disaster.setDisasterType(request.getDisasterType());
        disaster.setDescription(request.getDescription());
        disaster.setSeverity(request.getSeverity());
        disaster.setLocation(request.getAddress());
        disaster.setAddress(request.getAddress());
        disaster.setLatitude(request.getLatitude() != null ? request.getLatitude() : 0.0);
        disaster.setLongitude(request.getLongitude() != null ? request.getLongitude() : 0.0);
        disaster.setDate(LocalDateTime.now());
        disaster.setStatus(DisasterStatus.PENDING);
        disaster.setPriority(request.getPriority() != null ? request.getPriority() : DisasterPriority.MEDIUM);
        disaster.setSource("PUBLIC");
        disaster.setReportId(generateReportId());
        disaster.setReporterName(request.getReporterName());
        disaster.setReporterMobile(request.getReporterMobile());
        disaster.setReporterEmail(request.getReporterEmail());

        Disaster saved = disasterRepository.save(disaster);
        persistAttachments(saved, request.getAttachments());
        addTimeline(saved, null, DisasterStatus.PENDING.name(), null, "Disaster reported by citizen");

        auditService.log("PUBLIC_REPORT", "Disaster", saved.getId(), saved.getReporterName(),
                "Citizen reported " + request.getDisasterType() + " at " + request.getAddress());

        String msg = "New citizen report " + saved.getReportId() + ": " + request.getDisasterType()
                + " at " + request.getAddress();
        userRepository.findByRole(Role.ADMIN).forEach(admin ->
                notificationService.createNotification(admin.getId(), "Public Disaster Report", msg, "ALERT"));

        smsProvider.sendSMS(request.getReporterMobile(),
                "Your disaster report was received. Report ID: " + saved.getReportId()
                        + ". Track it at the Disaster Management portal.");
        if (request.getReporterEmail() != null && !request.getReporterEmail().isBlank()) {
            emailProvider.sendEmail(request.getReporterEmail(), "Disaster Report Received - " + saved.getReportId(),
                    "Thank you " + request.getReporterName() + ". Your report " + saved.getReportId()
                            + " has been received and is being reviewed.");
        }

        broadcastDisaster(saved);
        return PublicReportDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public PublicReportDTO getPublicReport(String reportId) {
        Disaster disaster = disasterRepository.findByReportId(reportId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No report found with ID: " + reportId));
        List<DisasterAttachment> attachments =
                disasterAttachmentRepository.findByDisasterIdOrderByCreatedAtAsc(disaster.getId());
        return PublicReportDTO.fromEntity(disaster, attachments, true);
    }

    @Transactional(readOnly = true)
    public List<StatusTimelineDTO> getPublicReportTimeline(String reportId) {
        Disaster disaster = disasterRepository.findByReportId(reportId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No report found with ID: " + reportId));
        return statusTimelineRepository.findByDisasterIdOrderByChangedAtAsc(disaster.getId())
                .stream().map(StatusTimelineDTO::fromEntity).toList();
    }

    private String generateReportId() {
        String date = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        StringBuilder suffix = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            suffix.append(ALPHANUM.charAt(ThreadLocalRandom.current().nextInt(ALPHANUM.length())));
        }
        return "DMS-" + date + "-" + suffix;
    }

    private void persistAttachments(Disaster disaster, List<AttachmentRequest> requests) {
        if (requests == null || requests.size() > 5) {
            throw new IllegalArgumentException("Maximum of 5 attachments allowed");
        }
        for (AttachmentRequest req : requests) {
            if (req.getDataUrl() == null || req.getDataUrl().isBlank()) continue;
            if (req.getDataUrl().length() > 7_000_000) {
                throw new IllegalArgumentException("Attachment exceeds the maximum allowed size");
            }
            if (req.getSize() != null && req.getSize() > MAX_ATTACHMENT_BYTES) {
                throw new IllegalArgumentException("Attachment exceeds the maximum allowed size");
            }
            String mime = req.getMimeType() != null ? req.getMimeType().trim().toLowerCase() : null;
            if (mime != null && !mime.isEmpty() && !ALLOWED_MIME.contains(mime)) {
                throw new IllegalArgumentException("Unsupported file type: " + req.getMimeType());
            }
            DisasterAttachment.Category category;
            try {
                category = DisasterAttachment.Category.valueOf(
                        req.getCategory() != null ? req.getCategory().trim().toUpperCase() : "IMAGE");
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Attachment category must be IMAGE, VIDEO or PDF");
            }
            disasterAttachmentRepository.save(new DisasterAttachment(
                    disaster, category, req.getFilename(), req.getMimeType(),
                    req.getSize(), req.getDataUrl()));
        }
    }
}
