package com.disaster.repository;

import com.disaster.entity.ResourceMovement;
import com.disaster.enums.ResourceMovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface ResourceMovementRepository extends JpaRepository<ResourceMovement, Long> {
    List<ResourceMovement> findByResourceIdOrderByOccurredAtDesc(Long resourceId);
    List<ResourceMovement> findAllByOrderByOccurredAtDesc();
    List<ResourceMovement> findByMovementTypeOrderByOccurredAtDesc(ResourceMovementType type);
    List<ResourceMovement> findByMissionIdOrderByOccurredAtDesc(Long missionId);

    @Transactional
    void deleteByResourceId(Long resourceId);
}
