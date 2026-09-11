package com.learning.springbootdemo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SoftwareEngineerRepository extends JpaRepository<SoftwareEngineer, Integer> {
    List<SoftwareEngineer> findAllByNameContaining(String name);

    List<SoftwareEngineer> findByTechStackContainingIgnoreCase(String techStack);
}
