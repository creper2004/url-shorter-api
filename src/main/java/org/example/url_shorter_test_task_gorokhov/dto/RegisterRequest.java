package org.example.url_shorter_test_task_gorokhov.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username cannot be blanked")
    @Size(min = 3, max = 50, message = "Username should be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password cannot be blanked")
    @Size(min = 6, max = 100, message = "Password should be between 6 and 100 characters")
    private String password;
}
