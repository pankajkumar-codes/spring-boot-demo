package com.learning.springbootdemo;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/software-engineers")
public class SoftwareEngineerController {

    private final SoftwareEngineerService softwareEngineerService;

    public SoftwareEngineerController(SoftwareEngineerService softwareEngineerService) {
        this.softwareEngineerService = softwareEngineerService;
    }

    @GetMapping
    public List<SoftwareEngineer> getEngineers() {
//        return List.of(
//                new SoftwareEngineer(1, "James", "js, node, react, tailwindcss"),
//                new SoftwareEngineer(2, "Jamila", "java, spring, springboot"));
        return softwareEngineerService.getAllSoftwareEngineers();
    }

    @GetMapping("{id}")
    public SoftwareEngineer getEngineersById(@PathVariable Integer id) {
        return softwareEngineerService.getSoftwareEngineersById(id);
    }

    @GetMapping("/search")
    public List<SoftwareEngineer> getEngineerSearchByName(@RequestParam String name) {
        return softwareEngineerService.getSoftwareEngineerByName(name);
    }

    @GetMapping("/filter")
    public List<SoftwareEngineer> findByTechStackContainingIgnoreCase(@RequestParam String techStack) {
        return softwareEngineerService.getSoftwareEngineerFilterByTechStack(techStack);
    }

    @PostMapping
    public void addNewSoftwareEngineer(@RequestBody SoftwareEngineer softwareEngineer) {
        softwareEngineerService.insertSoftwareEngineer(softwareEngineer);
    }
}
