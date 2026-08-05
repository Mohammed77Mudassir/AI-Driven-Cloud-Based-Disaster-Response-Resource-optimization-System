package com.disaster.repository;

import com.disaster.entity.Shelter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShelterRepository extends JpaRepository<Shelter, Long> {
    List<Shelter> findByNameContainingIgnoreCase(String name);
}
