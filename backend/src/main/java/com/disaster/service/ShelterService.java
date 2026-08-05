package com.disaster.service;

import com.disaster.dto.ShelterDTO;
import com.disaster.entity.Shelter;
import com.disaster.exception.ResourceNotFoundException;
import com.disaster.repository.ShelterRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ShelterService {
    private final ShelterRepository shelterRepository;
    
    public ShelterService(ShelterRepository shelterRepository) {
        this.shelterRepository = shelterRepository;
    }
    
    public List<ShelterDTO> getAll() {
        return shelterRepository.findAll().stream().map(ShelterDTO::fromEntity).toList();
    }
    
    public ShelterDTO getById(Long id) {
        return ShelterDTO.fromEntity(shelterRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Shelter not found")));
    }
    
    public ShelterDTO create(ShelterDTO dto) {
        Shelter s = Shelter.builder()
            .name(dto.getName())
            .capacity(dto.getCapacity())
            .occupancy(dto.getOccupancy())
            .foodAvailable(dto.isFoodAvailable())
            .waterAvailable(dto.isWaterAvailable())
            .medicalKits(dto.getMedicalKits())
            .powerAvailable(dto.isPowerAvailable())
            .contact(dto.getContact())
            .latitude(dto.getLatitude())
            .longitude(dto.getLongitude())
            .address(dto.getAddress())
            .build();
        return ShelterDTO.fromEntity(shelterRepository.save(s));
    }
    
    public ShelterDTO update(Long id, ShelterDTO dto) {
        Shelter s = shelterRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Shelter not found"));
        s.setName(dto.getName());
        s.setCapacity(dto.getCapacity());
        s.setOccupancy(dto.getOccupancy());
        s.setFoodAvailable(dto.isFoodAvailable());
        s.setWaterAvailable(dto.isWaterAvailable());
        s.setMedicalKits(dto.getMedicalKits());
        s.setPowerAvailable(dto.isPowerAvailable());
        s.setContact(dto.getContact());
        s.setLatitude(dto.getLatitude());
        s.setLongitude(dto.getLongitude());
        s.setAddress(dto.getAddress());
        return ShelterDTO.fromEntity(shelterRepository.save(s));
    }
    
    public void delete(Long id) {
        shelterRepository.deleteById(id);
    }
}
