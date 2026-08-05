package com.disaster.service;

import com.disaster.dto.CaseStudyDTO;
import com.disaster.dto.CaseStudyRequest;
import java.util.List;

public interface CaseStudyService {
    List<CaseStudyDTO> getAllCaseStudies();
    CaseStudyDTO getCaseStudyById(Long id);
    CaseStudyDTO createCaseStudy(CaseStudyRequest request);
    CaseStudyDTO updateCaseStudy(Long id, CaseStudyRequest request);
    void deleteCaseStudy(Long id);
    List<CaseStudyDTO> searchByYear(int year);
    List<CaseStudyDTO> searchByDisasterType(String type);
    List<CaseStudyDTO> searchByLocation(String location);
    List<CaseStudyDTO> getComparisonData(String disasterType, Integer yearFrom, Integer yearTo);
}
