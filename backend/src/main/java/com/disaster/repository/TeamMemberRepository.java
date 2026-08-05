package com.disaster.repository;

import com.disaster.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByTeamId(Long teamId);
    List<TeamMember> findByTeamIdOrderByIdAsc(Long teamId);
    List<TeamMember> findByAvailableTrue();
    List<TeamMember> findByTeamIdAndAvailableTrue(Long teamId);
    long countByTeamIdAndAvailableTrue(Long teamId);
    long countByTeamId(Long teamId);
    List<TeamMember> findByTeamIdAndIsLeaderTrue(Long teamId);
}
