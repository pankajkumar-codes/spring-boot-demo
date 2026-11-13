package com.learning.springbootdemo;

import org.springframework.data.domain.PageRequest;
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

    public List<SoftwareEngineer> getSoftwareEngineersPage(int page, int size) {
        return softwareEngineerRepository.findAll(PageRequest.of(page, size)).getContent();
    }

    public SoftwareEngineer getSoftwareEngineersById(Integer id) {
        return softwareEngineerRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    public List<SoftwareEngineer> getSoftwareEngineerByName(String name) {
        return softwareEngineerRepository.findAllByNameContaining(name);
    }

    public List<SoftwareEngineer> getSoftwareEngineerFilterByTechStack(String techStack) {
        return softwareEngineerRepository.findByTechStackContainingIgnoreCase(techStack);
    }

    public SoftwareEngineer insertSoftwareEngineer(SoftwareEngineerRequest request) {
        return softwareEngineerRepository.save(new SoftwareEngineer(null, request.name(), request.techStack()));
    }

    public SoftwareEngineer replaceSoftwareEngineer(Integer id, SoftwareEngineerRequest request) {
        SoftwareEngineer existing = getSoftwareEngineersById(id);
        existing.setName(request.name());
        existing.setTechStack(request.techStack());
        return softwareEngineerRepository.save(existing);
    }

    public void deleteSoftwareEngineer(Integer id) {
        if (!softwareEngineerRepository.existsById(id)) {
            throw notFound(id);
        }
        softwareEngineerRepository.deleteById(id);
    }

    private static ResponseStatusException notFound(Integer id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Software Engineer with ID " + id + " not found");
    }
}
