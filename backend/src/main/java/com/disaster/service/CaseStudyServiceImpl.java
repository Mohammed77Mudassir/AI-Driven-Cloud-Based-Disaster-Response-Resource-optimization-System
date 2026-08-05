package com.disaster.service;

import com.disaster.dto.CaseStudyDTO;
import com.disaster.dto.CaseStudyRequest;
import com.disaster.entity.CaseStudy;
import com.disaster.exception.ResourceNotFoundException;
import com.disaster.repository.CaseStudyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CaseStudyServiceImpl implements CaseStudyService {

    private final CaseStudyRepository caseStudyRepository;

    public CaseStudyServiceImpl(CaseStudyRepository caseStudyRepository) {
        this.caseStudyRepository = caseStudyRepository;
    }

    @Override
    public List<CaseStudyDTO> getAllCaseStudies() {
        return caseStudyRepository.findAll().stream()
                .map(CaseStudyDTO::fromEntity).toList();
    }

    @Override
    public CaseStudyDTO getCaseStudyById(Long id) {
        CaseStudy cs = caseStudyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case study not found with id: " + id));
        return CaseStudyDTO.fromEntity(cs);
    }

    @Override
    @Transactional
    public CaseStudyDTO createCaseStudy(CaseStudyRequest request) {
        CaseStudy cs = new CaseStudy();
        cs.setTitle(request.getTitle());
        cs.setDisasterType(request.getDisasterType());
        cs.setLocation(request.getLocation());
        cs.setDisasterYear(request.getYear());
        cs.setSeverity(request.getSeverity());
        cs.setDescription(request.getDescription());
        cs.setLessonsLearned(request.getLessonsLearned());
        cs.setEstimatedDamage(request.getEstimatedDamage());
        cs.setAffectedPopulation(request.getAffectedPopulation());
        cs.setResourcesUsed(request.getResourcesUsed());
        cs.setResponseTimeHours(request.getResponseTimeHours());
        cs.setRecoveryTimeDays(request.getRecoveryTimeDays());
        cs.setRecommendations(request.getRecommendations());
        cs.setCreatedAt(LocalDateTime.now());
        return CaseStudyDTO.fromEntity(caseStudyRepository.save(cs));
    }

    @Override
    @Transactional
    public CaseStudyDTO updateCaseStudy(Long id, CaseStudyRequest request) {
        CaseStudy cs = caseStudyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case study not found with id: " + id));
        if (request.getTitle() != null) cs.setTitle(request.getTitle());
        if (request.getDisasterType() != null) cs.setDisasterType(request.getDisasterType());
        if (request.getLocation() != null) cs.setLocation(request.getLocation());
        cs.setDisasterYear(request.getYear());
        if (request.getSeverity() != null) cs.setSeverity(request.getSeverity());
        if (request.getDescription() != null) cs.setDescription(request.getDescription());
        if (request.getLessonsLearned() != null) cs.setLessonsLearned(request.getLessonsLearned());
        cs.setEstimatedDamage(request.getEstimatedDamage());
        cs.setAffectedPopulation(request.getAffectedPopulation());
        cs.setResourcesUsed(request.getResourcesUsed());
        cs.setResponseTimeHours(request.getResponseTimeHours());
        cs.setRecoveryTimeDays(request.getRecoveryTimeDays());
        if (request.getRecommendations() != null) cs.setRecommendations(request.getRecommendations());
        return CaseStudyDTO.fromEntity(caseStudyRepository.save(cs));
    }

    @Override
    @Transactional
    public void deleteCaseStudy(Long id) {
        CaseStudy cs = caseStudyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case study not found with id: " + id));
        caseStudyRepository.delete(cs);
    }

    @Override
    public List<CaseStudyDTO> searchByYear(int year) {
        return caseStudyRepository.findByDisasterYear(year).stream()
                .map(CaseStudyDTO::fromEntity).toList();
    }

    @Override
    public List<CaseStudyDTO> searchByDisasterType(String type) {
        return caseStudyRepository.findByDisasterType(type).stream()
                .map(CaseStudyDTO::fromEntity).toList();
    }

    @Override
    public List<CaseStudyDTO> searchByLocation(String location) {
        return caseStudyRepository.findByLocationContainingIgnoreCase(location).stream()
                .map(CaseStudyDTO::fromEntity).toList();
    }

    @Override
    public List<CaseStudyDTO> getComparisonData(String disasterType, Integer yearFrom, Integer yearTo) {
        List<CaseStudy> studies;
        if (disasterType != null && !disasterType.isEmpty() && yearFrom != null && yearTo != null) {
            studies = caseStudyRepository.findByDisasterTypeAndDisasterYearBetween(disasterType, yearFrom, yearTo);
        } else if (disasterType != null && !disasterType.isEmpty()) {
            studies = caseStudyRepository.findByDisasterType(disasterType);
        } else if (yearFrom != null && yearTo != null) {
            studies = caseStudyRepository.findAll().stream()
                    .filter(cs -> cs.getDisasterYear() >= yearFrom && cs.getDisasterYear() <= yearTo)
                    .toList();
        } else {
            studies = caseStudyRepository.findAll();
        }
        return studies.stream().map(CaseStudyDTO::fromEntity).toList();
    }
}
