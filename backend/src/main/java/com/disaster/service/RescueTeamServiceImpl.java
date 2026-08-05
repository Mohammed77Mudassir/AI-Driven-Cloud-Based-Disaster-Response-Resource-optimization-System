package com.disaster.service;

import com.disaster.dto.RescueTeamDTO;
import com.disaster.dto.TeamAvailabilityDTO;
import com.disaster.dto.TeamMemberDTO;
import com.disaster.entity.Disaster;
import com.disaster.entity.RescueEquipment;
import com.disaster.entity.RescueMission;
import com.disaster.entity.RescueTeam;
import com.disaster.entity.TeamMember;
import com.disaster.enums.EquipmentStatus;
import com.disaster.enums.MissionStatus;
import com.disaster.enums.TeamStatus;
import com.disaster.enums.VehicleStatus;
import com.disaster.exception.ResourceNotFoundException;
import com.disaster.repository.DisasterRepository;
import com.disaster.repository.RescueEquipmentRepository;
import com.disaster.repository.RescueMissionRepository;
import com.disaster.repository.RescueTeamRepository;
import com.disaster.repository.RescueVehicleRepository;
import com.disaster.repository.TeamMemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class RescueTeamServiceImpl implements RescueTeamService {

    private final RescueTeamRepository rescueTeamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final DisasterRepository disasterRepository;
    private final RescueVehicleRepository vehicleRepository;
    private final RescueEquipmentRepository equipmentRepository;
    private final RescueMissionRepository missionRepository;
    private final DisasterService disasterService;

    public RescueTeamServiceImpl(RescueTeamRepository rescueTeamRepository,
                                 TeamMemberRepository teamMemberRepository,
                                 DisasterRepository disasterRepository,
                                 RescueVehicleRepository vehicleRepository,
                                 RescueEquipmentRepository equipmentRepository,
                                 RescueMissionRepository missionRepository,
                                 DisasterService disasterService) {
        this.rescueTeamRepository = rescueTeamRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.disasterRepository = disasterRepository;
        this.vehicleRepository = vehicleRepository;
        this.equipmentRepository = equipmentRepository;
        this.missionRepository = missionRepository;
        this.disasterService = disasterService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RescueTeamDTO> getAllTeams() {
        return rescueTeamRepository.findAll().stream()
                .map(RescueTeamDTO::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RescueTeamDTO getTeamById(Long id) {
        return RescueTeamDTO.fromEntity(findTeam(id));
    }

    @Override
    @Transactional
    public RescueTeamDTO createTeam(RescueTeamDTO dto) {
        if (dto.getTeamName() == null || dto.getTeamName().isBlank()) {
            throw new IllegalArgumentException("Team name is required");
        }
        RescueTeam team = new RescueTeam();
        team.setTeamName(dto.getTeamName());
        team.setTeamLeader(dto.getTeamLeader());
        team.setMembers(dto.getMembers());
        team.setVehicles(dto.getVehicles());
        team.setEquipment(dto.getEquipment());
        team.setStatus(dto.getStatus() != null ? validateStatus(dto.getStatus()) : TeamStatus.AVAILABLE.name());
        team.setLocation(dto.getLocation());
        team.setLatitude(dto.getLatitude());
        team.setLongitude(dto.getLongitude());
        team.setContactNumber(dto.getContactNumber());
        team.setMemberCount(dto.getMemberCount());
        team.setSpecialty(dto.getSpecialty());
        team.setMaxCapacity(dto.getMaxCapacity());
        return RescueTeamDTO.fromEntity(rescueTeamRepository.save(team));
    }

    @Override
    @Transactional
    public RescueTeamDTO updateTeam(Long id, RescueTeamDTO dto) {
        RescueTeam team = findTeam(id);
        if (dto.getTeamName() != null) {
            if (dto.getTeamName().isBlank()) throw new IllegalArgumentException("Team name cannot be blank");
            team.setTeamName(dto.getTeamName());
        }
        if (dto.getTeamLeader() != null) team.setTeamLeader(dto.getTeamLeader());
        if (dto.getMembers() != null) team.setMembers(dto.getMembers());
        if (dto.getVehicles() != null) team.setVehicles(dto.getVehicles());
        if (dto.getEquipment() != null) team.setEquipment(dto.getEquipment());
        if (dto.getStatus() != null) team.setStatus(validateStatus(dto.getStatus()));
        if (dto.getLocation() != null) team.setLocation(dto.getLocation());
        team.setLatitude(dto.getLatitude());
        team.setLongitude(dto.getLongitude());
        if (dto.getContactNumber() != null) team.setContactNumber(dto.getContactNumber());
        if (dto.getMemberCount() > 0) team.setMemberCount(dto.getMemberCount());
        if (dto.getSpecialty() != null) team.setSpecialty(dto.getSpecialty());
        if (dto.getMaxCapacity() > 0) team.setMaxCapacity(dto.getMaxCapacity());
        return RescueTeamDTO.fromEntity(rescueTeamRepository.save(team));
    }

    @Override
    @Transactional
    public void deleteTeam(Long id) {
        RescueTeam team = findTeam(id);
        teamMemberRepository.deleteAll(teamMemberRepository.findByTeamId(id));
        rescueTeamRepository.delete(team);
    }

    @Override
    @Transactional
    public RescueTeamDTO assignToDisaster(Long teamId, Long disasterId) {
        RescueTeam team = findTeam(teamId);
        Disaster disaster = disasterRepository.findById(disasterId)
                .orElseThrow(() -> new ResourceNotFoundException("Disaster not found with id: " + disasterId));

        if (team.getAssignedDisaster() != null && !team.getAssignedDisaster().getId().equals(disasterId)) {
            disasterService.recordAssignment(team.getAssignedDisaster().getId(), team, "system", "RELEASED");
        }

        team.setAssignedDisaster(disaster);
        team.setStatus(TeamStatus.DEPLOYED.name());
        team.setDeployedAt(LocalDateTime.now());
        team.setReturnedAt(null);
        RescueTeamDTO saved = RescueTeamDTO.fromEntity(rescueTeamRepository.save(team));
        disasterService.recordAssignment(disasterId, team, "system", "ASSIGNED");
        return saved;
    }

    @Override
    @Transactional
    public RescueTeamDTO updateTeamStatus(Long teamId, String status) {
        RescueTeam team = findTeam(teamId);
        String valid = validateStatus(status);
        team.setStatus(valid);
        if (TeamStatus.RETURNED.name().equalsIgnoreCase(valid)) {
            team.setReturnedAt(LocalDateTime.now());
            if (team.getAssignedDisaster() != null) {
                disasterService.recordAssignment(team.getAssignedDisaster().getId(), team, "system", "RELEASED");
            }
            team.setAssignedDisaster(null);
        }
        return RescueTeamDTO.fromEntity(rescueTeamRepository.save(team));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RescueTeamDTO> getTeamsByStatus(String status) {
        return rescueTeamRepository.findByStatus(validateStatus(status)).stream()
                .map(RescueTeamDTO::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RescueTeamDTO> getTeamsByDisaster(Long disasterId) {
        return rescueTeamRepository.findByAssignedDisasterId(disasterId).stream()
                .map(RescueTeamDTO::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamMemberDTO> getTeamMembers(Long teamId) {
        return teamMemberRepository.findByTeamIdOrderByIdAsc(teamId).stream()
                .map(TeamMemberDTO::fromEntity).toList();
    }

    @Override
    @Transactional
    public TeamMemberDTO addTeamMember(Long teamId, TeamMemberDTO dto) {
        RescueTeam team = findTeam(teamId);
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Member name is required");
        }
        if (team.getMaxCapacity() > 0 && team.getMemberCount() >= team.getMaxCapacity()) {
            throw new IllegalArgumentException("Team has reached its maximum capacity of " + team.getMaxCapacity());
        }
        TeamMember member = new TeamMember();
        member.setName(dto.getName());
        member.setRole(dto.getRole());
        member.setSpeciality(dto.getSpeciality());
        member.setPhone(dto.getPhone());
        member.setAvailable(true);
        member.setLeader(dto.isLeader());
        member.setSkills(dto.getSkills());
        member.setCertifications(dto.getCertifications());
        member.setJoinedAt(LocalDateTime.now());
        member.setTeam(team);
        TeamMember saved = teamMemberRepository.save(member);

        if (dto.isLeader()) {
            promoteLeader(team, saved);
        }
        team.setMemberCount((int) teamMemberRepository.countByTeamId(teamId));
        rescueTeamRepository.save(team);
        return TeamMemberDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public TeamMemberDTO updateTeamMember(Long memberId, TeamMemberDTO dto) {
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found with id: " + memberId));
        if (dto.getName() != null) {
            if (dto.getName().isBlank()) throw new IllegalArgumentException("Member name cannot be blank");
            member.setName(dto.getName());
        }
        if (dto.getRole() != null) member.setRole(dto.getRole());
        if (dto.getSpeciality() != null) member.setSpeciality(dto.getSpeciality());
        if (dto.getPhone() != null) member.setPhone(dto.getPhone());
        if (dto.getSkills() != null) member.setSkills(dto.getSkills());
        if (dto.getCertifications() != null) member.setCertifications(dto.getCertifications());
        if (dto.isLeader()) {
            RescueTeam team = member.getTeam();
            if (team != null) promoteLeader(team, member);
        } else {
            member.setLeader(false);
        }
        return TeamMemberDTO.fromEntity(teamMemberRepository.save(member));
    }

    @Override
    @Transactional
    public void removeTeamMember(Long memberId) {
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found with id: " + memberId));
        RescueTeam team = member.getTeam();
        if (member.isLeader() && team != null && member.getName().equals(team.getTeamLeader())) {
            team.setTeamLeader(null);
            rescueTeamRepository.save(team);
        }
        teamMemberRepository.delete(member);
        if (team != null) {
            team.setMemberCount((int) teamMemberRepository.countByTeamId(team.getId()));
            rescueTeamRepository.save(team);
        }
    }

    @Override
    @Transactional
    public RescueTeamDTO assignLeader(Long teamId, Long memberId) {
        RescueTeam team = findTeam(teamId);
        TeamMember member = teamMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Team member not found with id: " + memberId));
        if (member.getTeam() == null || !member.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("Member does not belong to team " + team.getTeamName());
        }
        promoteLeader(team, member);
        team.setMemberCount((int) teamMemberRepository.countByTeamId(teamId));
        rescueTeamRepository.save(team);
        return RescueTeamDTO.fromEntity(team);
    }

    private void promoteLeader(RescueTeam team, TeamMember newLeader) {
        teamMemberRepository.findByTeamIdAndIsLeaderTrue(team.getId()).forEach(m -> {
            if (!m.getId().equals(newLeader.getId())) {
                m.setLeader(false);
                teamMemberRepository.save(m);
            }
        });
        newLeader.setLeader(true);
        newLeader.setRole("Leader");
        teamMemberRepository.save(newLeader);
        team.setTeamLeader(newLeader.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public TeamAvailabilityDTO getTeamAvailability(Long teamId) {
        RescueTeam team = findTeam(teamId);
        return buildAvailability(team);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamAvailabilityDTO> getAvailabilityOverview() {
        return rescueTeamRepository.findAll().stream().map(this::buildAvailability).toList();
    }

    private TeamAvailabilityDTO buildAvailability(RescueTeam team) {
        TeamAvailabilityDTO dto = new TeamAvailabilityDTO();
        dto.setTeamId(team.getId());
        dto.setTeamName(team.getTeamName());
        dto.setTeamStatus(team.getStatus());
        dto.setTotalMembers((int) teamMemberRepository.countByTeamId(team.getId()));
        dto.setAvailableMembers((int) teamMemberRepository.countByTeamIdAndAvailableTrue(team.getId()));
        dto.setTotalVehicles((int) vehicleRepository.findByTeamId(team.getId()).size());
        dto.setAvailableVehicles((int) vehicleRepository.countByTeamIdAndStatus(team.getId(), VehicleStatus.AVAILABLE));
        List<RescueEquipment> equipmentList = equipmentRepository.findByTeamId(team.getId());
        dto.setTotalEquipmentItems(equipmentList.stream().mapToInt(e -> e.getAvailableQuantity() + e.getDeployedQuantity() + e.getInMaintenanceQuantity()).sum());
        dto.setAvailableEquipmentItems(equipmentList.stream().mapToInt(RescueEquipment::getAvailableQuantity).sum());
        boolean teamReady = team.getStatus() != null
                && (TeamStatus.AVAILABLE.name().equals(team.getStatus()) || TeamStatus.STANDING_BY.name().equals(team.getStatus()));
        dto.setDeployable(teamReady && dto.getAvailableMembers() > 0);
        missionRepository.findByTeamIdAndStatusInOrderByIdDesc(team.getId(),
                        List.of(MissionStatus.PENDING, MissionStatus.ASSIGNED, MissionStatus.IN_PROGRESS))
                .stream().findFirst().ifPresent(mission -> {
                    dto.setActiveMissionId(mission.getId());
                    dto.setActiveMissionCode(mission.getMissionCode());
                });
        return dto;
    }

    private RescueTeam findTeam(Long id) {
        return rescueTeamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rescue team not found with id: " + id));
    }

    private String validateStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Team status is required");
        }
        boolean valid = Arrays.stream(TeamStatus.values())
                .anyMatch(s -> s.name().equalsIgnoreCase(status));
        if (!valid) {
            throw new IllegalArgumentException("Invalid team status: " + status + ". Allowed values: "
                    + Arrays.toString(TeamStatus.values()));
        }
        return status.toUpperCase();
    }
}
