package org.example.url_shorter_test_task_gorokhov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {

    private String error;
    private String message;

}