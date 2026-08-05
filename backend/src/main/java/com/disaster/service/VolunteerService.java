package com.disaster.service;

import com.disaster.dto.VolunteerDTO;
import com.disaster.entity.Volunteer;
import com.disaster.entity.Disaster;
import com.disaster.exception.ResourceNotFoundException;
import com.disaster.repository.VolunteerRepository;
import com.disaster.repository.DisasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class VolunteerService {
    private final VolunteerRepository volunteerRepository;
    private final DisasterRepository disasterRepository;
    
    public VolunteerService(VolunteerRepository volunteerRepository, DisasterRepository disasterRepository) {
        this.volunteerRepository = volunteerRepository;
        this.disasterRepository = disasterRepository;
    }
    
    @Transactional(readOnly = true)
    public List<VolunteerDTO> getAll() {
        return volunteerRepository.findAll().stream().map(VolunteerDTO::fromEntity).toList();
    }
    
    @Transactional(readOnly = true)
    public VolunteerDTO getById(Long id) {
        return VolunteerDTO.fromEntity(volunteerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Volunteer not found")));
    }
    
    public VolunteerDTO create(VolunteerDTO dto) {
        Volunteer v = new Volunteer();
        v.setName(dto.getName());
        v.setEmail(dto.getEmail());
        v.setPhone(dto.getPhone());
        v.setSkills(dto.getSkills());
        v.setAvailable(dto.isAvailable());
        v.setAttended(dto.isAttended());
        v.setLatitude(dto.getLatitude());
        v.setLongitude(dto.getLongitude());
        return VolunteerDTO.fromEntity(volunteerRepository.save(v));
    }
    
    public VolunteerDTO update(Long id, VolunteerDTO dto) {
        Volunteer v = volunteerRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Volunteer not found"));
        v.setName(dto.getName());
        v.setEmail(dto.getEmail());
        v.setPhone(dto.getPhone());
        v.setSkills(dto.getSkills());
        v.setAvailable(dto.isAvailable());
        v.setAttended(dto.isAttended());
        v.setLatitude(dto.getLatitude());
        v.setLongitude(dto.getLongitude());
        if (dto.getAssignedDisasterId() != null) {
            Disaster d = disasterRepository.findById(dto.getAssignedDisasterId()).orElse(null);
            v.setAssignedDisaster(d);
        }
        return VolunteerDTO.fromEntity(volunteerRepository.save(v));
    }
    
    public void delete(Long id) {
        volunteerRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public List<VolunteerDTO> getAvailable() {
        return volunteerRepository.findByAvailableTrue().stream().map(VolunteerDTO::fromEntity).toList();
    }
}
