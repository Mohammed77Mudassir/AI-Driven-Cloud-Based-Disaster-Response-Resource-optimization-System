package com.disaster.repository;

import com.disaster.entity.DisasterComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DisasterCommentRepository extends JpaRepository<DisasterComment, Long> {
    List<DisasterComment> findByDisasterIdOrderByCreatedAtDesc(Long disasterId);
    void deleteByDisasterId(Long disasterId);
}
