package com.disaster.repository;

import com.disaster.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VolunteerRepository extends JpaRepository<Volunteer, Long> {
    List<Volunteer> findByAvailableTrue();
    List<Volunteer> findByAssignedDisasterId(Long disasterId);
}
