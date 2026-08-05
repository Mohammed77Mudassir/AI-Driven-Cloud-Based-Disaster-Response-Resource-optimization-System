package com.disaster.repository;

import com.disaster.entity.DisasterAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DisasterAttachmentRepository extends JpaRepository<DisasterAttachment, Long> {
    List<DisasterAttachment> findByDisasterIdOrderByCreatedAtAsc(Long disasterId);
    void deleteByDisasterId(Long disasterId);
}
