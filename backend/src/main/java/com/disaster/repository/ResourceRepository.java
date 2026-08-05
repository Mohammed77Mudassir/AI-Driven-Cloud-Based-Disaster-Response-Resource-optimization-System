package com.disaster.repository;

import com.disaster.entity.Resource;
import com.disaster.enums.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
    List<Resource> findByAvailableTrue();
    List<Resource> findByResourceType(ResourceType resourceType);
    List<Resource> findByAssignedDisasterId(Long disasterId);
    List<Resource> findByAssignedMissionId(Long missionId);
}
