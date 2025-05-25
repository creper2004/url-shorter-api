package org.example.url_shorter_test_task_gorokhov.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ExpandRequest(@NotBlank(message = "Short URL is blank" )
                            @Size(max= 255, message = "Size of short URL cannot be more than 255 characters")
                            @Pattern(regexp = "^(http|https)://.*$", message = "Invalid URL format")
                            String shortUrl) { }