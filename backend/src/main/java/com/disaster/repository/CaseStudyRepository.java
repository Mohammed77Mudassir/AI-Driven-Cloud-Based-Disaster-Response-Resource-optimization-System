package com.disaster.repository;

import com.disaster.entity.CaseStudy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CaseStudyRepository extends JpaRepository<CaseStudy, Long> {
    List<CaseStudy> findByDisasterYear(int year);
    List<CaseStudy> findByDisasterType(String disasterType);
    List<CaseStudy> findByLocationContainingIgnoreCase(String location);
    List<CaseStudy> findByDisasterTypeAndDisasterYearBetween(String disasterType, int yearFrom, int yearTo);
}
