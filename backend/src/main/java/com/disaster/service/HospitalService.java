package com.disaster.service;

import com.disaster.dto.HospitalDTO;
import com.disaster.entity.Hospital;
import com.disaster.exception.ResourceNotFoundException;
import com.disaster.repository.HospitalRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HospitalService {
    private final HospitalRepository hospitalRepository;
    
    public HospitalService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }
    
    public List<HospitalDTO> getAll() {
        return hospitalRepository.findAll().stream().map(HospitalDTO::fromEntity).toList();
    }
    
    public HospitalDTO getById(Long id) {
        return HospitalDTO.fromEntity(hospitalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Hospital not found")));
    }
    
    public HospitalDTO create(HospitalDTO dto) {
        Hospital h = Hospital.builder()
            .name(dto.getName())
            .availableBeds(dto.getAvailableBeds())
            .icuBeds(dto.getIcuBeds())
            .doctorsAvailable(dto.getDoctorsAvailable())
            .emergencyContact(dto.getEmergencyContact())
            .bloodBank(dto.isBloodBank())
            .latitude(dto.getLatitude())
            .longitude(dto.getLongitude())
            .address(dto.getAddress())
            .build();
        return HospitalDTO.fromEntity(hospitalRepository.save(h));
    }
    
    public HospitalDTO update(Long id, HospitalDTO dto) {
        Hospital h = hospitalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Hospital not found"));
        h.setName(dto.getName());
        h.setAvailableBeds(dto.getAvailableBeds());
        h.setIcuBeds(dto.getIcuBeds());
        h.setDoctorsAvailable(dto.getDoctorsAvailable());
        h.setEmergencyContact(dto.getEmergencyContact());
        h.setBloodBank(dto.isBloodBank());
        h.setLatitude(dto.getLatitude());
        h.setLongitude(dto.getLongitude());
        h.setAddress(dto.getAddress());
        return HospitalDTO.fromEntity(hospitalRepository.save(h));
    }
    
    public void delete(Long id) {
        hospitalRepository.deleteById(id);
    }
}
