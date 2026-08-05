package com.disaster.repository;

import com.disaster.entity.Disaster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;
import java.util.Optional;

public interface DisasterRepository extends JpaRepository<Disaster, Long>, JpaSpecificationExecutor<Disaster> {
    @EntityGraph(attributePaths = "user")
    List<Disaster> findByUserIdOrderByDateDesc(Long userId);

    @EntityGraph(attributePaths = "user")
    List<Disaster> findAllByOrderByDateDesc();

    Optional<Disaster> findByReportId(String reportId);

    /**
     * Overrides the inherited {@code findAll(Specification, Pageable)} to
     * eagerly fetch the {@code user} association and avoid N+1 selects when
     * mapping paginated disaster results.
     */
    @Override
    @EntityGraph(attributePaths = "user")
    Page<Disaster> findAll(Specification<Disaster> spec, Pageable pageable);
}
