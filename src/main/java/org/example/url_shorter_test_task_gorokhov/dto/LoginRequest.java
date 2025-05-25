package org.example.url_shorter_test_task_gorokhov.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Username cannot be blanked")
    private String username;

    @NotBlank(message = "Password cannot be blanked")
    private String password;
}
