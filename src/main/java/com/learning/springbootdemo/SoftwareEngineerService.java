package com.learning.springbootdemo;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SoftwareEngineerService {

    private final SoftwareEngineerRepository softwareEngineerRepository;

    public SoftwareEngineerService(SoftwareEngineerRepository softwareEngineerRepository) {
        this.softwareEngineerRepository = softwareEngineerRepository;
    }

    public List<SoftwareEngineer> getAllSoftwareEngineers() {
        return softwareEngineerRepository.findAll();
    }

    public SoftwareEngineer getSoftwareEngineersById(Integer id) {
        return softwareEngineerRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Software Engineer with ID " + id + " not found"));
    }

    public List<SoftwareEngineer> getSoftwareEngineerByName(String name) {
        return softwareEngineerRepository.findAllByNameContaining(name);
    }

    public List<SoftwareEngineer> getSoftwareEngineerFilterByTechStack(String techStack) {
        return softwareEngineerRepository.findByTechStackContainingIgnoreCase(techStack);
    }

    public void insertSoftwareEngineer(SoftwareEngineer softwareEngineer) {
        softwareEngineerRepository.save(softwareEngineer);
    }
}
