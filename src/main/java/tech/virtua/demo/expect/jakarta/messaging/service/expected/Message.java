package tech.virtua.demo.expect.jakarta.messaging.service.expected;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Message(@NotBlank @Size(min = 5) String text) { }
