package com.learning.springbootdemo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@Validated
@RequestMapping("api/v1/software-engineers")
public class SoftwareEngineerController {

    private final SoftwareEngineerService softwareEngineerService;

    public SoftwareEngineerController(SoftwareEngineerService softwareEngineerService) {
        this.softwareEngineerService = softwareEngineerService;
    }

    /** Without page and size the full list is returned (backwards compatible); with them, one page. */
    @GetMapping
    public List<SoftwareEngineer> getEngineers(
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(100) Integer size) {
        if (page == null && size == null) {
            return softwareEngineerService.getAllSoftwareEngineers();
        }
        return softwareEngineerService.getSoftwareEngineersPage(page == null ? 0 : page, size == null ? 20 : size);
    }

    @GetMapping("{id}")
    public SoftwareEngineer getEngineersById(@PathVariable Integer id) {
        return softwareEngineerService.getSoftwareEngineersById(id);
    }

    @GetMapping("/search")
    public List<SoftwareEngineer> getEngineerSearchByName(@RequestParam @NotBlank @Size(max = 100) String name) {
        return softwareEngineerService.getSoftwareEngineerByName(name);
    }

    @GetMapping("/filter")
    public List<SoftwareEngineer> findByTechStackContainingIgnoreCase(
            @RequestParam @NotBlank @Size(max = 100) String techStack) {
        return softwareEngineerService.getSoftwareEngineerFilterByTechStack(techStack);
    }

    @PostMapping
    public ResponseEntity<SoftwareEngineer> addNewSoftwareEngineer(@Valid @RequestBody SoftwareEngineerRequest request) {
        SoftwareEngineer created = softwareEngineerService.insertSoftwareEngineer(request);
        return ResponseEntity.created(URI.create("/api/v1/software-engineers/" + created.getId())).body(created);
    }

    @PutMapping("{id}")
    public SoftwareEngineer replaceSoftwareEngineer(@PathVariable Integer id,
                                                    @Valid @RequestBody SoftwareEngineerRequest request) {
        return softwareEngineerService.replaceSoftwareEngineer(id, request);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteSoftwareEngineer(@PathVariable Integer id) {
        softwareEngineerService.deleteSoftwareEngineer(id);
        return ResponseEntity.noContent().build();
    }
}
