package com.learning.springbootdemo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body for create and replace. The id is never read from the client. */
public record SoftwareEngineerRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 100) String techStack) {
}
