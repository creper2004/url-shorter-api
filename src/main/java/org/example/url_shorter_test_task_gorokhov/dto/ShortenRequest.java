package org.example.url_shorter_test_task_gorokhov.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ShortenRequest(@NotBlank(message = "Full URL is blank")
                             @Size(max = 2048, message = "Size of full URL cannot be more than 2048 characters")
                             @Pattern(regexp = "^(http|https)://.*$", message = "Invalid URL format")
                             String fullUrl) { }